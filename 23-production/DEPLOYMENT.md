# Run the operations example

Use these commands inside `23-production`. Docker Desktop must be running. This is a local deployment exercise, not a public production service.

## Build and start

PowerShell:

```powershell
.\mvnw.cmd clean verify
docker compose up -d --build
docker compose exec ollama ollama pull llama3.2:3b
```

macOS/Linux:

```bash
sh ./mvnw clean verify
docker compose up -d --build
docker compose exec ollama ollama pull llama3.2:3b
```

The model pull is explicit: it downloads several gigabytes and needs enough local RAM to run. No cloud API is called. The app has a 768 MB container memory limit and uses a non-root user. Ollama has its own persistent model volume.

## Check it

- Status: http://localhost:8103/
- Liveness: http://localhost:8103/actuator/health/liveness
- Readiness: http://localhost:8103/actuator/health/readiness
- Chat: `POST http://localhost:8103/api/chat` using the README request.

Readiness checks that Ollama's `/api/tags` responds. Confirm the installed model separately with `docker compose exec ollama ollama list` and a real chat request. A green readiness check alone is not proof that inference will work.

Inspect startup or failures with `docker compose logs --tail 50 app`. `docker compose stop ollama` should make readiness DOWN while liveness stays UP. Use `docker compose start ollama` to restore it.

## Stop and clean up

`docker compose down` stops the services and keeps downloaded models. Optional `docker compose down -v` also removes this project's model volume. Stop the app before rebuilding its JAR.

## Before a real deployment

Add authentication and TLS. Restrict metrics to operators. Keep credentials in your deployment secret store. Pin container image digests instead of the teaching tags, especially Ollama's floating `latest` tag. Configure hardware budgets, request limits, alerting, rollout and rollback, and run live checks with your chosen model before accepting traffic. This lesson does not automatically deploy anything to a cloud account.
