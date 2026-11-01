package com.example.lesson09;

import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import org.springframework.ai.tool.annotation.Tool;
import java.util.concurrent.ConcurrentHashMap;

@RestController
class LessonController {
    final InventoryService inventory=new InventoryService();
    private final ChatClient client;
    LessonController(ChatClient.Builder builder) { client=builder.defaultAdvisors(new ToolBudgetAdvisor()).build(); }
    @PostMapping("/api/chat") Map<String,String> chat(@Valid @RequestBody Question q,Principal user) {
        // Only a read-only tool is exposed. Owner identity comes from authentication.
        return Map.of("answer",client.prompt().user(q.question()).advisors(a -> a.param("modelCallCount",new java.util.concurrent.atomic.AtomicInteger())).tools(new StockTools(inventory,user.getName())).call().content());
    }
    @PostMapping("/api/admin/orders") Map<String,Object> approve(@Valid @RequestBody Order order,Principal staff) {
        if(!order.confirm()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Explicit confirmation is required.");
        return inventory.order(staff.getName(),order.requestId(),order.quantity());
    }
    record Question(@NotBlank @Size(max=1000) String question) {}
    record Order(@NotBlank @Pattern(regexp="[A-Za-z0-9-]{1,40}") String requestId,@Min(1) @Max(5) int quantity,boolean confirm) {}
}
class InventoryService {
    private int stock=10;
    private final Map<String,Map<String,Object>> orders=new ConcurrentHashMap<>();
    int stock() { return stock; }
    synchronized Map<String,Object> order(String actor,String id,int quantity) {
        String key=actor+":"+id;
        if(orders.containsKey(key)) return orders.get(key);
        if(quantity<1 || quantity>5 || quantity>stock) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid order quantity.");
        stock-=quantity;
        var result=Map.<String,Object>of("requestId",id,"remaining",stock);
        orders.put(key,result); return result;
    }
}
class StockTools {
    private final InventoryService inventory; private final String actor;
    StockTools(InventoryService inventory,String actor) { this.inventory=inventory; this.actor=actor; }
    @Tool(description="Read PEN stock. This tool cannot place or approve an order.")
    public Map<String,Object> stock() { return Map.of("sku","PEN","available",inventory.stock(),"requestedBy",actor); }
}

class ToolBudgetAdvisor implements org.springframework.ai.chat.client.advisor.api.CallAdvisor {
    public String getName() { return "bounded-model-calls"; }
    public int getOrder() { return org.springframework.core.Ordered.LOWEST_PRECEDENCE-100; }
    public org.springframework.ai.chat.client.ChatClientResponse adviseCall(
            org.springframework.ai.chat.client.ChatClientRequest request,
            org.springframework.ai.chat.client.advisor.api.CallAdvisorChain chain) {
        var count=(java.util.concurrent.atomic.AtomicInteger)request.context().get("modelCallCount");
        if(count==null) {
            count=new java.util.concurrent.atomic.AtomicInteger();
            request=request.mutate().context("modelCallCount",count).build();
        }
        if(count.incrementAndGet()>4) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Tool loop exceeded four model calls.");
        return chain.nextCall(request);
    }
}

@org.springframework.context.annotation.Configuration
class SecurityConfig {
    @org.springframework.context.annotation.Bean
    org.springframework.security.core.userdetails.UserDetailsService users() {
        // Public classroom credentials. Replace this identity store before real deployment.
        return new org.springframework.security.provisioning.InMemoryUserDetailsManager(
            org.springframework.security.core.userdetails.User.withUsername("alice").password("{noop}local-alice").roles("STUDENT").build(),
            org.springframework.security.core.userdetails.User.withUsername("bob").password("{noop}local-bob").roles("STUDENT").build(),
            org.springframework.security.core.userdetails.User.withUsername("staff").password("{noop}local-staff").roles("STAFF").build());
    }
    @org.springframework.context.annotation.Bean
    org.springframework.security.web.SecurityFilterChain filter(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a.requestMatchers("/","/actuator/health/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("STAFF").anyRequest().authenticated())
            .httpBasic(org.springframework.security.config.Customizer.withDefaults()).build();
    }
}
