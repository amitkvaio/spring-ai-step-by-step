package com.example.lesson19;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class LessonTest {
    @Test void registersAndExecutesARealSpringToolCallback() {
        var callbacks=new LessonController().shopTools().getToolCallbacks();
        assertThat(callbacks).hasSize(1); assertThat(callbacks[0].getToolDefinition().name()).isEqualTo("price");
        assertThat(callbacks[0].call("{\"sku\":\"PEN\"}")).isEqualTo("10");
    }
    @Test void rejectsUnknownProducts() { assertThatThrownBy(() -> new ShopTools().price("OTHER")).isInstanceOf(IllegalArgumentException.class); }
}
