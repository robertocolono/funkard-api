##############################################
# 🧠 FUNKARD BACKEND — Makefile Commands
# Usa:
#   make dev   → avvia in modalità sviluppo
#   make prod  → avvia in modalità produzione
##############################################

.PHONY: dev prod logs stop clean

# === 🧪 Dev mode (CORS aperto, log dettagliati)
dev:
	@echo "🚀 Avvio Funkard API in modalità DEV..."
	mvn spring-boot:run -Dspring-boot.run.profiles=dev

# === 🚀 Production mode (simula Render)
prod:
	@echo "🌍 Avvio Funkard API in modalità PROD..."
	mvn spring-boot:run -Dspring-boot.run.profiles=prod

# === 📜 Mostra log live
logs:
	@echo "📄 Monitoraggio log di Funkard API..."
	tail -f logs/funkard-api.log

# === 🧹 Pulisce i build files
clean:
	@echo "🧽 Pulizia target e cache Maven..."
	mvn clean

# === 🛑 Stoppa processi Java (se serve)
stop:
	@echo "🛑 Arresto di eventuali processi Java in corso..."
	@pkill -f "java" || true
