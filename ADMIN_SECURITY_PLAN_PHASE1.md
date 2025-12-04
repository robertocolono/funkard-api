# 🔐 Piano di Sicurezza Admin - FASE 1
## Analisi Completa Sistema Autenticazione Admin Funkard

---

## 1. Panorama Generale

Il backend Funkard attualmente gestisce l'autenticazione admin attraverso **due sistemi paralleli**:

1. **Sistema Nuovo** (sessioni httpOnly):
   - Autenticazione basata su cookie `admin_session` (httpOnly, 4 ore)
   - Validazione tramite `AdminSessionFilter` che popola `SecurityContext`
   - Controllo permessi con `@PreAuthorize("hasRole('ADMIN')")`
   - Utilizzato da ~51 endpoint completamente migrati

2. **Sistema Legacy** (token statici):
   - Autenticazione tramite header `X-Admin-Token` con token statico da variabile d'ambiente `admin.token`
   - Oppure `Authorization: Bearer {SUPER_ADMIN_TOKEN}` da env
   - Oppure token `accessToken` salvato in database `admin_users`
   - Utilizzato da ~20 endpoint non ancora migrati

**Principali Rischi Attuali**:
- **Vulnerabilità critiche**: 4 controller completamente pubblici (nessuna autenticazione) che espongono dati sensibili o permettono operazioni distruttive
- **Token statici hardcoded**: Se le variabili d'ambiente vengono compromesse, accesso completo al sistema admin
- **Token database non funzionanti**: Dopo onboarding, `accessToken = null`, quindi endpoint legacy che usano `getByToken()` non funzionano più
- **Inconsistenza autenticazione**: Alcuni endpoint usano sessioni, altri token statici, creando confusione e possibili bypass
- **Duplicazione controller**: Stessi path gestiti da controller diversi con logiche diverse

---

## 2. Tabella Endpoint `/api/admin/**`

| Controller | Metodo | HTTP | Path | Sicurezza Attuale | Severità | Note |
|------------|--------|------|------|-------------------|----------|------|
| `AdminAuthController` | `tokenCheck` | GET | `/api/admin/auth/token-check?token=...` | `permitAll()` (pubblico) | BASSO | Validazione token onboarding, non espone dati sensibili |
| `AdminAuthController` | `onboardingComplete` | POST | `/api/admin/auth/onboarding-complete` | `permitAll()` (pubblico) | MEDIO | Completa onboarding, richiede token monouso valido |
| `AdminAuthController` | `login` | POST | `/api/admin/auth/login` | `permitAll()` (pubblico) | MEDIO | Login pubblico corretto, crea sessione dopo validazione credenziali |
| `AdminAuthController` | `logout` | POST | `/api/admin/auth/logout` | Sessione httpOnly | BASSO | Invalida sessione, richiede cookie valido |
| `AdminAuthController` | `me` | GET | `/api/admin/auth/me` | Sessione httpOnly + `@PreAuthorize` implicito | MEDIO | Dati admin corrente, protetto da sessione |
| `AdminAuthController` | `validateToken` | GET | `/api/admin/auth/token/{token}` | Pubblico | MEDIO | Valida token legacy, espone dati utente |
| `AdminAuthController` | `createUser` | POST | `/api/admin/auth/users/create` | `X-Admin-Token` + `getByToken(accessToken)` | CRITICO | Crea utente admin, token legacy non funziona dopo onboarding |
| `AdminAuthController` | `regenerateToken` | PATCH | `/api/admin/auth/users/{id}/regenerate-token` | `X-Admin-Token` + `getByToken(accessToken)` | CRITICO | Rigenera token utente, token legacy non funziona |
| `AdminAuthController` | `deactivateUser` | PATCH | `/api/admin/auth/users/{id}/deactivate` | `X-Admin-Token` + `getByToken(accessToken)` | CRITICO | Disattiva utente admin, token legacy non funziona |
| `AdminAuthController` | `activateUser` | PATCH | `/api/admin/auth/users/{id}/activate` | `X-Admin-Token` + `getByToken(accessToken)` | CRITICO | Riattiva utente admin, token legacy non funziona |
| `AdminAuthController` | `changeRole` | PATCH | `/api/admin/auth/users/{id}/role` | `X-Admin-Token` + `getByToken(accessToken)` | CRITICO | Cambia ruolo utente, token legacy non funziona |
| `AdminAuthController` | `listTeam` | GET | `/api/admin/auth/team/list` | `X-Admin-Token` + `getByToken(accessToken)` | MEDIO | Lista team admin, token legacy non funziona |
| `AdminAuthController` | `diagnostic` | GET | `/api/admin/auth/diagnostic` | Pubblico | BASSO | Diagnostica Super Admin, solo lettura |
| `AdminAuthController` | `verifyAndFix` | POST | `/api/admin/auth/verify-and-fix` | Pubblico | MEDIO | Fix Super Admin, potrebbe essere pericoloso se pubblico |
| `AdminAuthController` | `createToken` | POST | `/api/admin/auth/tokens/create` | `X-Admin-Token` + `getByToken(accessToken)` | CRITICO | Crea token di ruolo, token legacy non funziona |
| `AdminAuthController` | `listTokens` | GET | `/api/admin/auth/tokens/list` | `X-Admin-Token` + `getByToken(accessToken)` | MEDIO | Lista token, token legacy non funziona |
| `AdminAuthController` | `disableToken` | POST | `/api/admin/auth/tokens/{id}/disable` | `X-Admin-Token` + `getByToken(accessToken)` | MEDIO | Disabilita token, token legacy non funziona |
| `AdminAuthController` | `regenerateToken` | POST | `/api/admin/auth/tokens/{id}/regenerate` | `X-Admin-Token` + `getByToken(accessToken)` | MEDIO | Rigenera token ruolo, token legacy non funziona |
| `AdminNotificationController` | `list` | GET | `/api/admin/notifications` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Lista notifiche, protetto correttamente |
| `AdminNotificationController` | `get` | GET | `/api/admin/notifications/{id}` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Dettaglio notifica, protetto correttamente |
| `AdminNotificationController` | `markRead` | POST | `/api/admin/notifications/{id}/read` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Marca letta, protetto correttamente |
| `AdminNotificationController` | `assign` | POST | `/api/admin/notifications/{id}/assign` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Assegna notifica, protetto correttamente |
| `AdminNotificationController` | `resolve` | POST | `/api/admin/notifications/{id}/resolve` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Risolvi notifica, protetto correttamente |
| `AdminNotificationController` | `archive` | POST | `/api/admin/notifications/{id}/archive` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Archivia notifica, protetto correttamente |
| `AdminNotificationController` | `stream` | GET | `/api/admin/notifications/stream` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | SSE notifiche, protetto correttamente |
| `AdminNotificationController` | `getUnreadCount` | GET | `/api/admin/notifications/unread-count` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Contatore non lette, protetto correttamente |
| `AdminNotificationController` | `getUnreadLatest` | GET | `/api/admin/notifications/unread-latest` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Ultime non lette, protetto correttamente |
| `AdminNotificationController` | `cleanup` | DELETE/POST | `/api/admin/notifications/cleanup` | `Bearer FUNKARD_CRON_SECRET` o `@PreAuthorize` | MEDIO | Cleanup notifiche, dual auth corretta |
| `AdminNotificationBatchController` | `batchResolve` | POST | `/api/admin/notifications/batch/resolve` | NESSUNA (PUBBLICO) | CRITICO | Risolve notifiche in batch, completamente pubblico |
| `AdminNotificationBatchController` | `batchArchive` | POST | `/api/admin/notifications/batch/archive` | NESSUNA (PUBBLICO) | CRITICO | Archivia notifiche in batch, completamente pubblico |
| `AdminNotificationBatchController` | `batchDelete` | DELETE | `/api/admin/notifications/batch/delete` | NESSUNA (PUBBLICO) | CRITICO | Elimina notifiche in batch, completamente pubblico |
| `AdminNotificationArchiveController` | `getArchivedNotifications` | GET | `/api/admin/notifications/archive` | NESSUNA (PUBBLICO) | MEDIO | Lista notifiche archiviate, completamente pubblico |
| `AdminNotificationArchiveController` | `deleteArchivedNotification` | DELETE | `/api/admin/notifications/delete/{id}` | NESSUNA (PUBBLICO) | CRITICO | Elimina notifica archiviata, completamente pubblico |
| `AdminNotificationActionController` | `archiveNotification` | PATCH | `/api/admin/notifications/archive/{id}` | NESSUNA (PUBBLICO) | CRITICO | Archivia notifica, completamente pubblico |
| `AdminNotificationStreamController` | `streamNotifications` | GET | `/api/admin/notifications/stream` | NESSUNA (PUBBLICO) | MEDIO | SSE notifiche (duplicato), completamente pubblico |
| `AdminNotificationStreamController` | `testSSE` | GET | `/api/admin/notifications/test` | NESSUNA (PUBBLICO) | BASSO | Test SSE, completamente pubblico |
| `AdminNotificationCleanupController` | `manualCleanup` | POST | `/api/admin/notifications/cleanup/manual` | NESSUNA (PUBBLICO) | CRITICO | Cleanup manuale notifiche, completamente pubblico |
| `AdminNotificationCleanupController` | `getCleanupStats` | GET | `/api/admin/notifications/cleanup/stats` | NESSUNA (PUBBLICO) | BASSO | Statistiche cleanup, completamente pubblico |
| `AdminNotificationCleanupController` | `testCleanup` | POST | `/api/admin/notifications/cleanup/test` | NESSUNA (PUBBLICO) | MEDIO | Test cleanup, completamente pubblico |
| `AdminNotificationCleanupController` | `getCleanupInfo` | GET | `/api/admin/notifications/cleanup/info` | NESSUNA (PUBBLICO) | BASSO | Info cleanup, completamente pubblico |
| `AdminSupportController` | `getAllTickets` | GET | `/api/admin/support/tickets` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Lista ticket, protetto correttamente |
| `AdminSupportController` | `getStats` | GET | `/api/admin/support/stats` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Statistiche supporto, protetto correttamente |
| `AdminSupportController` | `replyToTicket` | POST | `/api/admin/support/reply/{id}` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Rispondi ticket, protetto correttamente |
| `AdminSupportController` | `resolveTicket` | POST | `/api/admin/support/resolve/{id}` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Risolvi ticket, protetto correttamente |
| `AdminSupportController` | `closeTicket` | POST | `/api/admin/support/close/{id}` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Chiudi ticket, protetto correttamente |
| `AdminSupportController` | `reopenTicket` | POST | `/api/admin/support/reopen/{id}` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Riapri ticket, protetto correttamente |
| `AdminSupportController` | `markMessagesAsRead` | POST | `/api/admin/support/{id}/mark-read` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Marca messaggi letti, protetto correttamente |
| `AdminSupportController` | `getNewMessagesCount` | GET | `/api/admin/support/new-messages-count` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Contatore nuovi messaggi, protetto correttamente |
| `AdminSupportController` | `assignTicket` | POST | `/api/admin/support/{id}/assign` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Assegna ticket, protetto correttamente |
| `AdminSupportController` | `releaseTicket` | POST | `/api/admin/support/{id}/release` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Rilascia ticket, protetto correttamente |
| `AdminSupportController` | `getAssignedTickets` | GET | `/api/admin/support/assigned/{supportEmail}` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Ticket assegnati, protetto correttamente |
| `AdminSupportController` | `getAssignedCount` | GET | `/api/admin/support/assigned-count` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Contatore assegnati, protetto correttamente |
| `AdminSupportCleanupController` | `cleanupOldMessages` | DELETE/POST | `/api/admin/support/cleanup` | `Bearer FUNKARD_CRON_SECRET` o `@PreAuthorize` | MEDIO | Cleanup messaggi supporto, dual auth corretta |
| `AdminStreamController` | `stream` | GET | `/api/admin/support/stream?userId={id}&role={role}` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | SSE stream supporto, protetto correttamente |
| `AdminStreamController` | `sendTestEvent` | POST | `/api/admin/support/stream/events` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | BASSO | Test invio eventi, protetto correttamente |
| `AdminStreamController` | `getStats` | GET | `/api/admin/support/stream/stats` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | BASSO | Statistiche connessioni, protetto correttamente |
| `AdminStatsController` | `getStats` | GET | `/api/admin/stats` | `@PreAuthorize("hasRole('ADMIN')")` + Sessione | MEDIO | Statistiche generali, protetto correttamente |
| `AdminDashboardController` | `getDashboard` | GET | `/api/admin/dashboard` | NESSUNA (PUBBLICO) | CRITICO | Dashboard aggregata con dati sensibili, completamente pubblico |
| `AdminDashboardController` | `cleanupOldNotifications` | DELETE | `/api/admin/dashboard/cleanup` | NESSUNA (PUBBLICO) | CRITICO | Cleanup notifiche, completamente pubblico |
| `AdminActionLogController` | `getHistory` | GET | `/api/admin/logs/{type}/{id}` | NESSUNA (PUBBLICO) | MEDIO | Storico azioni admin, completamente pubblico |
| `AdminActionLogController` | `cleanupOldLogs` | DELETE/POST | `/api/admin/logs/cleanup` | `Bearer FUNKARD_CRON_SECRET` o `@PreAuthorize` | MEDIO | Cleanup log, dual auth corretta |
| `MaintenanceController` | `cleanupLogs` | POST | `/api/admin/maintenance/cleanup-logs` | `Bearer FUNKARD_CRON_SECRET` o `@PreAuthorize` | MEDIO | Cleanup log manutenzione, dual auth corretta |
| `SystemMaintenanceController` | `updateCleanupStatus` | POST | `/api/admin/system/cleanup/status` | `Bearer FUNKARD_CRON_SECRET` o `@PreAuthorize` | BASSO | Aggiorna status cleanup, dual auth corretta |
| `SystemMaintenanceController` | `getCleanupStatus` | GET | `/api/admin/system/cleanup/status` | NESSUNA (PUBBLICO) | BASSO | Status cleanup, solo lettura |
| `FranchiseAdminController` | `getAllFranchisesAndProposals` | GET | `/api/admin/franchises` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Lista franchise, protetto correttamente |
| `FranchiseAdminController` | `approveProposal` | POST | `/api/admin/franchises/approve/{proposalId}` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Approva proposta, protetto correttamente |
| `FranchiseAdminController` | `rejectProposal` | POST | `/api/admin/franchises/reject/{proposalId}` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Rifiuta proposta, protetto correttamente |
| `FranchiseAdminController` | `disableFranchise` | PATCH | `/api/admin/franchises/{id}/disable` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Disabilita franchise, protetto correttamente |
| `FranchiseAdminController` | `enableFranchise` | PATCH | `/api/admin/franchises/{id}/enable` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Riabilita franchise, protetto correttamente |
| `FranchiseAdminController` | `createFranchise` | POST | `/api/admin/franchises/add` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Crea franchise, protetto correttamente |
| `AdminFranchiseController` | `getAllFranchises` | GET | `/api/admin/franchises/catalog` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Lista franchise catalogo, protetto correttamente |
| `AdminFranchiseController` | `createFranchise` | POST | `/api/admin/franchises/catalog` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Crea franchise catalogo, protetto correttamente |
| `AdminFranchiseController` | `updateFranchise` | PUT | `/api/admin/franchises/catalog/{id}` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Aggiorna franchise, protetto correttamente |
| `AdminFranchiseController` | `deleteFranchise` | DELETE | `/api/admin/franchises/catalog/{id}` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Elimina franchise, protetto correttamente |
| `AdminFranchiseController` | `getStats` | GET | `/api/admin/franchises/catalog/stats` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Statistiche catalogo, protetto correttamente |
| `AdminPendingValueController` | `getPendingValues` | GET | `/api/admin/pending-values` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Lista proposte pending, protetto correttamente |
| `AdminPendingValueController` | `approvePendingValue` | POST | `/api/admin/pending-values/{id}/approve` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Approva proposta, protetto correttamente |
| `AdminPendingValueController` | `rejectPendingValue` | DELETE | `/api/admin/pending-values/{id}` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Rifiuta proposta, protetto correttamente |
| `AdminPendingValueController` | `getStats` | GET | `/api/admin/pending-values/stats` | `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Statistiche proposte, protetto correttamente |
| `AdminEmailLogController` | `getEmailLogs` | GET | `/api/admin/email-logs` | `@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Lista log email, protetto correttamente |
| `AdminEmailLogController` | `getEmailLogDetail` | GET | `/api/admin/email-logs/{id}` | `@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Dettaglio log email, protetto correttamente |
| `AdminEmailLogController` | `getEmailLogStats` | GET | `/api/admin/email-logs/stats` | `@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPERVISOR')")` + Sessione | MEDIO | Statistiche email logs, protetto correttamente |
| `CookieLogAdminController` | `getLogs` | GET | `/api/admin/cookies/logs` | `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")` + Sessione | MEDIO | Log consenso cookie, protetto correttamente |
| `CookieLogAdminController` | `exportLogs` | GET | `/api/admin/cookies/logs/export` | `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")` + Sessione | MEDIO | Export log cookie, protetto correttamente |
| `EmailTemplateTestController` | `testAllTemplates` | POST | `/api/admin/email-templates/test/all` | `@PreAuthorize("hasRole('SUPER_ADMIN')")` + Sessione | BASSO | Test template, protetto correttamente |
| `EmailTemplateTestController` | `testVariableSubstitution` | POST | `/api/admin/email-templates/test/variables` | `@PreAuthorize("hasRole('SUPER_ADMIN')")` + Sessione | BASSO | Test variabili, protetto correttamente |
| `AdminValuationController` | `getOverview` | GET | `/api/admin/valuation/overview` | `X-Admin-Token` (statico `@Value("${admin.token}")`) | MEDIO | Overview valutazioni, token statico legacy |
| `AdminController` | `getPendingItems` | GET | `/api/admin/valuation/pending` | `X-Admin-Token` (statico `@Value("${admin.token}")`) | MEDIO | Elementi pending, token statico legacy |
| `AdminController` | `checkAdmin` | GET | `/api/admin/valuation/check` | `X-Admin-Token` (statico `@Value("${admin.token}")`) | BASSO | Check accesso, token statico legacy |
| `RolePermissionController` | `getUserPermissions` | GET | `/api/admin/roles/permissions/{userEmail}` | `X-Admin-Token` (statico `@Value("${admin.token}")`) | MEDIO | Permessi utente, token statico legacy |
| `RolePermissionController` | `checkTicketPermissions` | POST | `/api/admin/roles/check-permissions` | `X-Admin-Token` (statico `@Value("${admin.token}")`) | MEDIO | Verifica permessi ticket, token statico legacy |
| `RolePermissionController` | `getAvailableRoles` | GET | `/api/admin/roles/available` | `X-Admin-Token` (statico `@Value("${admin.token}")`) | BASSO | Ruoli disponibili, token statico legacy |
| `AdminLegacyAuthController` | `ping` | GET | `/api/admin/ping` | `Authorization: Bearer {admin.token}` (statico) | BASSO | Test autenticazione legacy, `@Deprecated` |
| `AccessRequestController` | `createRequest` | POST | `/api/admin/access-requests/create` | Pubblico | BASSO | Crea richiesta accesso, pubblico corretto |
| `AccessRequestController` | `getPendingRequests` | GET | `/api/admin/access-requests/pending` | `X-Admin-Token` + `getByToken(accessToken)` | MEDIO | Lista richieste pending, token legacy non funziona |
| `AccessRequestController` | `approveRequest` | POST | `/api/admin/access-requests/approve/{id}` | `X-Admin-Token` + `getByToken(accessToken)` | CRITICO | Approva richiesta, crea AdminUser, token legacy non funziona |
| `AccessRequestController` | `rejectRequest` | POST | `/api/admin/access-requests/reject/{id}` | `X-Admin-Token` + `getByToken(accessToken)` | MEDIO | Rifiuta richiesta, token legacy non funziona |
| `AdminTokenController` | `listTokens` | GET | `/api/admin/tokens` | NESSUNA (PUBBLICO) | CRITICO | Lista tutti i token accesso, completamente pubblico |
| `AdminTokenController` | `generateToken` | POST | `/api/admin/tokens/generate?role={ROLE}` | `Authorization: Bearer {SUPER_ADMIN_TOKEN}` (env) | CRITICO | Genera token ruolo, usa env var |
| `AdminTokenController` | `validateToken` | GET | `/api/admin/tokens/validate/{token}` | NESSUNA (PUBBLICO) | BASSO | Valida token, pubblico ma solo lettura |
| `AdminAccessController` | `generateToken` | POST | `/api/admin/access/generate?role={ROLE}` | `Authorization: Bearer {SUPER_ADMIN_TOKEN}` (env) | CRITICO | Genera token (duplicato), usa env var |
| `AdminAccessController` | `submitRequest` | POST | `/api/admin/access/request` | Pubblico | BASSO | Richiesta accesso (duplicato), pubblico corretto |
| `AdminAccessController` | `getRequests` | GET | `/api/admin/access/requests` | `Authorization: Bearer {SUPER_ADMIN_TOKEN}` (env) | MEDIO | Lista richieste, usa env var |
| `AdminAccessController` | `approveRequest` | POST | `/api/admin/access/approve/{id}` | `Authorization: Bearer {SUPER_ADMIN_TOKEN}` (env) | CRITICO | Approva richiesta (duplicato), usa env var |
| `AdminAccessController` | `rejectRequest` | POST | `/api/admin/access/reject/{id}` | `Authorization: Bearer {SUPER_ADMIN_TOKEN}` (env) | MEDIO | Rifiuta richiesta (duplicato), usa env var |
| `AdminAccessController` | `listTokens` | GET | `/api/admin/access/tokens` | `Authorization: Bearer {SUPER_ADMIN_TOKEN}` (env) | MEDIO | Lista token (duplicato), usa env var |
| `AdminTicketAssignmentController` | `assignTicket` | POST | `/api/admin/tickets/{id}/assign` | NESSUNA (PUBBLICO) | CRITICO | Assegna ticket, completamente pubblico |
| `AdminTicketAssignmentController` | `releaseTicket` | POST | `/api/admin/tickets/{id}/release` | NESSUNA (PUBBLICO) | CRITICO | Rilascia ticket, completamente pubblico |
| `AdminTicketAssignmentController` | `assignTicketWithRole` | POST | `/api/admin/tickets/{id}/assign-with-role` | NESSUNA (PUBBLICO) | CRITICO | Assegna ticket con ruolo, completamente pubblico |
| `AdminTicketAssignmentController` | `releaseTicketWithRole` | POST | `/api/admin/tickets/{id}/release-with-role` | NESSUNA (PUBBLICO) | CRITICO | Rilascia ticket con ruolo, completamente pubblico |
| `AdminTicketAssignmentController` | `getAssignmentStats` | GET | `/api/admin/tickets/assignment-stats` | NESSUNA (PUBBLICO) | MEDIO | Statistiche assegnazioni, completamente pubblico |
| `GradeLensAdminController` | `metrics` | GET | `/api/admin/gradelens/metrics` | NESSUNA (PUBBLICO) | MEDIO | Metriche GradeLens, completamente pubblico |
| `GradeLensAdminController` | `purgeExpired` | POST | `/api/admin/gradelens/purge` | NESSUNA (PUBBLICO) | CRITICO | Elimina report scaduti, completamente pubblico |
| `AdminFixController` | `fixOnboardingColumn` | GET | `/api/admin/fix/onboarding-column` | `Bearer FUNKARD_CRON_SECRET` | BASSO | Fix temporaneo DB, protetto da cron secret |
| `AdminCleanupController` | `manualCleanup` | POST | `/api/admin/cleanup/manual` | NESSUNA (PUBBLICO) | CRITICO | Cleanup manuale, completamente pubblico |
| `AdminCleanupController` | `getCleanupStats` | GET | `/api/admin/cleanup/stats` | NESSUNA (PUBBLICO) | BASSO | Statistiche cleanup, completamente pubblico |
| `AdminCleanupController` | `testCleanup` | POST | `/api/admin/cleanup/test` | NESSUNA (PUBBLICO) | MEDIO | Test cleanup, completamente pubblico |

---

## 3. Endpoint Pericolosi (Priorità FASE 1)

### 🔴 CRITICI - Accesso Pubblico a Operazioni Sensibili

1. **`GET /api/admin/dashboard`**
   - **Perché è pericoloso**: Espone dashboard aggregata con dati sensibili (statistiche utenti, prodotti, carte, notifiche, mercato, grading, supporto)
   - **Rischio**: Chiunque può vedere dati aggregati del sistema senza autenticazione

2. **`DELETE /api/admin/dashboard/cleanup`**
   - **Perché è pericoloso**: Elimina notifiche archiviate senza autenticazione
   - **Rischio**: Chiunque può cancellare dati storici

3. **`POST /api/admin/notifications/batch/resolve`**
   - **Perché è pericoloso**: Risolve notifiche in batch senza autenticazione
   - **Rischio**: Chiunque può modificare stato di notifiche multiple

4. **`POST /api/admin/notifications/batch/archive`**
   - **Perché è pericoloso**: Archivia notifiche in batch senza autenticazione
   - **Rischio**: Chiunque può modificare stato di notifiche multiple

5. **`DELETE /api/admin/notifications/batch/delete`**
   - **Perché è pericoloso**: Elimina notifiche in batch senza autenticazione
   - **Rischio**: Chiunque può cancellare notifiche del sistema

6. **`DELETE /api/admin/notifications/delete/{id}`**
   - **Perché è pericoloso**: Elimina notifica archiviata senza autenticazione
   - **Rischio**: Chiunque può cancellare notifiche

7. **`PATCH /api/admin/notifications/archive/{id}`**
   - **Perché è pericoloso**: Archivia notifica senza autenticazione
   - **Rischio**: Chiunque può modificare stato notifiche

8. **`POST /api/admin/notifications/cleanup/manual`**
   - **Perché è pericoloso**: Esegue cleanup manuale notifiche senza autenticazione
   - **Rischio**: Chiunque può cancellare dati storici

9. **`GET /api/admin/tokens`**
   - **Perché è pericoloso**: Lista tutti i token di accesso admin senza autenticazione
   - **Rischio**: Chiunque può vedere token attivi (anche se preview)

10. **`POST /api/admin/tickets/{id}/assign`**
    - **Perché è pericoloso**: Assegna ticket supporto senza autenticazione
    - **Rischio**: Chiunque può assegnare ticket a qualsiasi email

11. **`POST /api/admin/tickets/{id}/release`**
    - **Perché è pericoloso**: Rilascia ticket senza autenticazione
    - **Rischio**: Chiunque può modificare assegnazioni ticket

12. **`POST /api/admin/tickets/{id}/assign-with-role`**
    - **Perché è pericoloso**: Assegna ticket con controllo ruolo senza autenticazione
    - **Rischio**: Chiunque può bypassare controlli ruolo

13. **`POST /api/admin/tickets/{id}/release-with-role`**
    - **Perché è pericoloso**: Rilascia ticket con controllo ruolo senza autenticazione
    - **Rischio**: Chiunque può bypassare controlli ruolo

14. **`POST /api/admin/gradelens/purge`**
    - **Perché è pericoloso**: Elimina report GradeLens scaduti senza autenticazione
    - **Rischio**: Chiunque può cancellare dati grading

15. **`POST /api/admin/cleanup/manual`**
    - **Perché è pericoloso**: Esegue cleanup manuale senza autenticazione
    - **Rischio**: Chiunque può cancellare dati storici

16. **`POST /api/admin/auth/verify-and-fix`**
    - **Perché è pericoloso**: Modifica Super Admin senza autenticazione
    - **Rischio**: Chiunque può modificare configurazione Super Admin

### 🟡 MEDI - Token Statici o Legacy Non Funzionanti

17. **Tutti gli endpoint in `AdminAuthController` che usano `X-Admin-Token` + `getByToken()`**
    - **Perché è pericoloso**: Dopo onboarding, `accessToken = null`, quindi questi endpoint non funzionano più
    - **Rischio**: Gestione utenti admin completamente rotta dopo onboarding

18. **`GET /api/admin/valuation/overview`**, **`GET /api/admin/valuation/pending`**
    - **Perché è pericoloso**: Usa token statico hardcoded in env, se compromesso accesso completo
    - **Rischio**: Se `admin.token` env leak, accesso a dati valutazioni

19. **`GET /api/admin/logs/{type}/{id}`**
    - **Perché è pericoloso**: Espone storico azioni admin senza autenticazione
    - **Rischio**: Chiunque può vedere log azioni sensibili

20. **`GET /api/admin/notifications/archive`**
    - **Perché è pericoloso**: Espone notifiche archiviate senza autenticazione
    - **Rischio**: Chiunque può vedere notifiche archiviate

---

## 4. Piano di Bonifica MINIMO (Senza Impatto su Flussi Esistenti)

### Top 5 Endpoint da Mettere in Sicurezza (Priorità Assoluta)

1. **`GET /api/admin/dashboard`**
2. **`DELETE /api/admin/dashboard/cleanup`**
3. **`POST /api/admin/notifications/batch/*`** (tutti e 3)
4. **`POST /api/admin/tickets/{id}/assign`** e **`POST /api/admin/tickets/{id}/release`**
5. **`GET /api/admin/tokens`**

---

### Piano Dettagliato per Endpoint NON Migrati

#### [PLAN] GET /api/admin/dashboard
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `getDashboard()`
  - Passo 2: Verificare che `AdminSessionFilter` popoli correttamente `SecurityContext` per questo path
  - Note: Endpoint già protetto da `adminSecurityFilterChain` che richiede `.authenticated()`, ma manca `@PreAuthorize` esplicito. Aggiunta non rompe nulla, solo aggiunge controllo esplicito.

#### [PLAN] DELETE /api/admin/dashboard/cleanup
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `cleanupOldNotifications()`
  - Passo 2: Verificare che funzioni con sessioni httpOnly
  - Note: Stesso approccio di `getDashboard()`, aggiunta non rompe flussi esistenti.

#### [PLAN] POST /api/admin/notifications/batch/resolve
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `batchResolve()`
  - Passo 2: Verificare che `AdminSessionFilter` gestisca correttamente questo path
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, possibile uso interno da tool admin. Verificare se frontend usa questo endpoint.

#### [PLAN] POST /api/admin/notifications/batch/archive
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `batchArchive()`
  - Passo 2: Stesso approccio di `batchResolve()`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, possibile uso interno.

#### [PLAN] DELETE /api/admin/notifications/batch/delete
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `batchDelete()`
  - Passo 2: Stesso approccio di `batchResolve()`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, possibile uso interno.

#### [PLAN] GET /api/admin/notifications/archive
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `getArchivedNotifications()`
  - Passo 2: Verificare che frontend usi questo endpoint o quello principale in `AdminNotificationController`
  - Note: Endpoint duplicato, verificare se è ancora usato. Se non usato, considerare rimozione invece di fix.

#### [PLAN] DELETE /api/admin/notifications/delete/{id}
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `deleteArchivedNotification()`
  - Passo 2: Verificare che frontend usi questo endpoint
  - Note: Endpoint duplicato, verificare se è ancora usato.

#### [PLAN] PATCH /api/admin/notifications/archive/{id}
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `archiveNotification()`
  - Passo 2: Verificare che frontend usi questo endpoint o quello principale
  - Note: Endpoint duplicato, verificare se è ancora usato.

#### [PLAN] GET /api/admin/notifications/stream (AdminNotificationStreamController)
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `streamNotifications()`
  - Passo 2: Verificare che frontend usi questo endpoint o quello principale in `AdminNotificationController`
  - Note: Endpoint duplicato, verificare se è ancora usato. Se non usato, considerare rimozione.

#### [PLAN] GET /api/admin/notifications/test
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: BASSO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `testSSE()`
  - Passo 2: Oppure rimuovere endpoint se non più necessario
  - Note: Endpoint di test, valutare se necessario in produzione.

#### [PLAN] POST /api/admin/notifications/cleanup/manual
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `manualCleanup()`
  - Passo 2: Verificare che non sia usato da cron (dovrebbe usare endpoint principale)
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, possibile uso da tool interni.

#### [PLAN] GET /api/admin/notifications/cleanup/stats
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: BASSO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `getCleanupStats()`
  - Note: Solo lettura, rischio basso ma meglio proteggere.

#### [PLAN] POST /api/admin/notifications/cleanup/test
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `testCleanup()`
  - Passo 2: Oppure rimuovere endpoint se non più necessario in produzione
  - Note: Endpoint di test, valutare se necessario.

#### [PLAN] GET /api/admin/notifications/cleanup/info
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: BASSO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `getCleanupInfo()`
  - Note: Solo lettura info, rischio basso.

#### [PLAN] GET /api/admin/logs/{type}/{id}
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `getHistory()`
  - Note: Espone log azioni, meglio proteggere.

#### [PLAN] GET /api/admin/system/cleanup/status
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: BASSO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `getCleanupStatus()`
  - Note: Solo lettura status, rischio basso ma meglio proteggere.

#### [PLAN] GET /api/admin/valuation/overview
- **Situazione attuale**: `X-Admin-Token` statico da `@Value("${admin.token}")`
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `@Value("${admin.token}")` e confronto token statico
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `getOverview()`
  - Passo 3: Verificare che frontend usi sessioni httpOnly
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, verificare che frontend non usi più `X-Admin-Token` per questo endpoint.

#### [PLAN] GET /api/admin/valuation/pending
- **Situazione attuale**: `X-Admin-Token` statico da `@Value("${admin.token}")`
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `@Value("${admin.token}")` e confronto token statico
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `getPendingItems()`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, verificare uso frontend.

#### [PLAN] GET /api/admin/valuation/check
- **Situazione attuale**: `X-Admin-Token` statico da `@Value("${admin.token}")`
- **Rischio**: BASSO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `@Value("${admin.token}")` e confronto token statico
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `checkAdmin()`
  - Passo 3: Oppure rimuovere endpoint se non più necessario (solo test)
  - Note: Endpoint di test, valutare se necessario.

#### [PLAN] GET /api/admin/roles/permissions/{userEmail}
- **Situazione attuale**: `X-Admin-Token` statico da `@Value("${admin.token}")`
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `@Value("${admin.token}")` e confronto token statico
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `getUserPermissions()`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, verificare uso frontend.

#### [PLAN] POST /api/admin/roles/check-permissions
- **Situazione attuale**: `X-Admin-Token` statico da `@Value("${admin.token}")`
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `@Value("${admin.token}")` e confronto token statico
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `checkTicketPermissions()`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, verificare uso frontend.

#### [PLAN] GET /api/admin/roles/available
- **Situazione attuale**: `X-Admin-Token` statico da `@Value("${admin.token}")`
- **Rischio**: BASSO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `@Value("${admin.token}")` e confronto token statico
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `getAvailableRoles()`
  - Note: Solo lettura ruoli, rischio basso.

#### [PLAN] GET /api/admin/ping
- **Situazione attuale**: `Authorization: Bearer {admin.token}` statico, `@Deprecated`
- **Rischio**: BASSO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere endpoint completamente (già deprecato)
  - Passo 2: Oppure aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` se necessario per compatibilità
  - Note: Endpoint deprecato, meglio rimuovere.

#### [PLAN] GET /api/admin/access-requests/pending
- **Situazione attuale**: `X-Admin-Token` + `getByToken(accessToken)` (non funziona dopo onboarding)
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `X-Admin-Token` header check
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `getPendingRequests()`
  - Passo 3: Rimuovere chiamata a `userService.getByToken()` e usare `SecurityContext` per ottenere admin corrente
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, possibile uso da tool interni. Modifica logica per usare sessioni invece di token legacy.

#### [PLAN] POST /api/admin/access-requests/approve/{id}
- **Situazione attuale**: `X-Admin-Token` + `getByToken(accessToken)` (non funziona dopo onboarding)
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `X-Admin-Token` header check
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `approveRequest()`
  - Passo 3: Rimuovere chiamata a `userService.getByToken()` e usare `SecurityContext` per ottenere admin corrente
  - Note: ⚠️ Richiede conferma prima di applicare modifiche. Modifica logica per usare sessioni invece di token legacy.

#### [PLAN] POST /api/admin/access-requests/reject/{id}
- **Situazione attuale**: `X-Admin-Token` + `getByToken(accessToken)` (non funziona dopo onboarding)
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `X-Admin-Token` header check
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `rejectRequest()`
  - Passo 3: Rimuovere chiamata a `userService.getByToken()` e usare `SecurityContext` per ottenere admin corrente
  - Note: ⚠️ Richiede conferma prima di applicare modifiche. Modifica logica per usare sessioni invece di token legacy.

#### [PLAN] GET /api/admin/tokens
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `listTokens()`
  - Passo 2: Verificare che frontend non usi questo endpoint senza autenticazione
  - Note: Espone lista token, critico proteggere.

#### [PLAN] POST /api/admin/tokens/generate?role={ROLE}
- **Situazione attuale**: `Authorization: Bearer {SUPER_ADMIN_TOKEN}` da env
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Mantenere `SUPER_ADMIN_TOKEN` env per compatibilità cron/tool esterni
  - Passo 2: Aggiungere anche supporto per sessioni: `@PreAuthorize("hasRole('SUPER_ADMIN')")` come alternativa
  - Passo 3: Logica dual: se `Authorization: Bearer {SUPER_ADMIN_TOKEN}` → OK, altrimenti verifica sessione + `@PreAuthorize`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, possibile uso da tool esterni. Mantenere env var come fallback.

#### [PLAN] GET /api/admin/tokens/validate/{token}
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: BASSO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `validateToken()`
  - Note: Solo validazione token, rischio basso ma meglio proteggere.

#### [PLAN] POST /api/admin/access/generate?role={ROLE}
- **Situazione attuale**: `Authorization: Bearer {SUPER_ADMIN_TOKEN}` da env
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Stesso approccio di `/api/admin/tokens/generate` (dual auth)
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` come alternativa
  - Note: Endpoint duplicato di `/api/admin/tokens/generate`, valutare unificazione.

#### [PLAN] GET /api/admin/access/requests
- **Situazione attuale**: `Authorization: Bearer {SUPER_ADMIN_TOKEN}` da env
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Stesso approccio di `/api/admin/tokens/generate` (dual auth)
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` come alternativa
  - Note: Endpoint duplicato di `/api/admin/access-requests/pending`, valutare unificazione.

#### [PLAN] POST /api/admin/access/approve/{id}
- **Situazione attuale**: `Authorization: Bearer {SUPER_ADMIN_TOKEN}` da env
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Stesso approccio di `/api/admin/tokens/generate` (dual auth)
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` come alternativa
  - Note: Endpoint duplicato di `/api/admin/access-requests/approve/{id}`, valutare unificazione.

#### [PLAN] POST /api/admin/access/reject/{id}
- **Situazione attuale**: `Authorization: Bearer {SUPER_ADMIN_TOKEN}` da env
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Stesso approccio di `/api/admin/tokens/generate` (dual auth)
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` come alternativa
  - Note: Endpoint duplicato di `/api/admin/access-requests/reject/{id}`, valutare unificazione.

#### [PLAN] GET /api/admin/access/tokens
- **Situazione attuale**: `Authorization: Bearer {SUPER_ADMIN_TOKEN}` da env
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Stesso approccio di `/api/admin/tokens/generate` (dual auth)
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` come alternativa
  - Note: Endpoint duplicato di `/api/admin/tokens`, valutare unificazione.

#### [PLAN] POST /api/admin/tickets/{id}/assign
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `assignTicket()`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, verificare se duplicato con `/api/admin/support/{id}/assign`.

#### [PLAN] POST /api/admin/tickets/{id}/release
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `releaseTicket()`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, verificare se duplicato con `/api/admin/support/{id}/release`.

#### [PLAN] POST /api/admin/tickets/{id}/assign-with-role
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `assignTicketWithRole()`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, verificare se ancora usato.

#### [PLAN] POST /api/admin/tickets/{id}/release-with-role
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `releaseTicketWithRole()`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, verificare se ancora usato.

#### [PLAN] GET /api/admin/tickets/assignment-stats
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `getAssignmentStats()`
  - Note: Solo lettura statistiche, rischio medio.

#### [PLAN] GET /api/admin/gradelens/metrics
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `metrics()`
  - Note: Solo lettura metriche, rischio medio.

#### [PLAN] POST /api/admin/gradelens/purge
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `purgeExpired()`
  - Note: Elimina dati, critico proteggere.

#### [PLAN] POST /api/admin/auth/verify-and-fix
- **Situazione attuale**: Pubblico
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `verifyAndFix()`
  - Passo 2: Oppure proteggere con `Bearer FUNKARD_CRON_SECRET` se usato da cron
  - Note: Modifica Super Admin, meglio proteggere.

#### [PLAN] POST /api/admin/cleanup/manual
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `manualCleanup()`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche, possibile uso da tool interni.

#### [PLAN] GET /api/admin/cleanup/stats
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: BASSO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `getCleanupStats()`
  - Note: Solo lettura statistiche, rischio basso.

#### [PLAN] POST /api/admin/cleanup/test
- **Situazione attuale**: Nessuna autenticazione, completamente pubblico
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Aggiungere `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` al metodo `testCleanup()`
  - Passo 2: Oppure rimuovere endpoint se non più necessario in produzione
  - Note: Endpoint di test, valutare se necessario.

#### [PLAN] POST /api/admin/auth/users/create
- **Situazione attuale**: `X-Admin-Token` + `getByToken(accessToken)` (non funziona dopo onboarding)
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `X-Admin-Token` header check
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `createUser()`
  - Passo 3: Rimuovere chiamata a `userService.getByToken()` e usare `SecurityContext` per ottenere admin corrente
  - Note: ⚠️ Richiede conferma prima di applicare modifiche. Modifica logica per usare sessioni invece di token legacy. Questo endpoint è fondamentale per gestione utenti admin.

#### [PLAN] PATCH /api/admin/auth/users/{id}/regenerate-token
- **Situazione attuale**: `X-Admin-Token` + `getByToken(accessToken)` (non funziona dopo onboarding)
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `X-Admin-Token` header check
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `regenerateToken()`
  - Passo 3: Rimuovere chiamata a `userService.getByToken()` e usare `SecurityContext`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche. Modifica logica per usare sessioni.

#### [PLAN] PATCH /api/admin/auth/users/{id}/deactivate
- **Situazione attuale**: `X-Admin-Token` + `getByToken(accessToken)` (non funziona dopo onboarding)
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `X-Admin-Token` header check
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `deactivateUser()`
  - Passo 3: Rimuovere chiamata a `userService.getByToken()` e usare `SecurityContext`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche. Modifica logica per usare sessioni.

#### [PLAN] PATCH /api/admin/auth/users/{id}/activate
- **Situazione attuale**: `X-Admin-Token` + `getByToken(accessToken)` (non funziona dopo onboarding)
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `X-Admin-Token` header check
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `activateUser()`
  - Passo 3: Rimuovere chiamata a `userService.getByToken()` e usare `SecurityContext`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche. Modifica logica per usare sessioni.

#### [PLAN] PATCH /api/admin/auth/users/{id}/role
- **Situazione attuale**: `X-Admin-Token` + `getByToken(accessToken)` (non funziona dopo onboarding)
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `X-Admin-Token` header check
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `changeRole()`
  - Passo 3: Rimuovere chiamata a `userService.getByToken()` e usare `SecurityContext`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche. Modifica logica per usare sessioni.

#### [PLAN] GET /api/admin/auth/team/list
- **Situazione attuale**: `X-Admin-Token` + `getByToken(accessToken)` (non funziona dopo onboarding)
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `X-Admin-Token` header check
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `listTeam()`
  - Passo 3: Rimuovere chiamata a `userService.getByToken()` e usare `SecurityContext`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche. Modifica logica per usare sessioni.

#### [PLAN] POST /api/admin/auth/tokens/create
- **Situazione attuale**: `X-Admin-Token` + `getByToken(accessToken)` (non funziona dopo onboarding)
- **Rischio**: CRITICO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `X-Admin-Token` header check
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `createToken()`
  - Passo 3: Rimuovere chiamata a `userService.getByToken()` e usare `SecurityContext`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche. Modifica logica per usare sessioni.

#### [PLAN] GET /api/admin/auth/tokens/list
- **Situazione attuale**: `X-Admin-Token` + `getByToken(accessToken)` (non funziona dopo onboarding)
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `X-Admin-Token` header check
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `listTokens()`
  - Passo 3: Rimuovere chiamata a `userService.getByToken()` e usare `SecurityContext`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche. Modifica logica per usare sessioni.

#### [PLAN] POST /api/admin/auth/tokens/{id}/disable
- **Situazione attuale**: `X-Admin-Token` + `getByToken(accessToken)` (non funziona dopo onboarding)
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `X-Admin-Token` header check
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `disableToken()`
  - Passo 3: Rimuovere chiamata a `userService.getByToken()` e usare `SecurityContext`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche. Modifica logica per usare sessioni.

#### [PLAN] POST /api/admin/auth/tokens/{id}/regenerate
- **Situazione attuale**: `X-Admin-Token` + `getByToken(accessToken)` (non funziona dopo onboarding)
- **Rischio**: MEDIO
- **Piano FASE 1 (SENZA rompere niente)**:
  - Passo 1: Rimuovere `X-Admin-Token` header check
  - Passo 2: Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` al metodo `regenerateToken()`
  - Passo 3: Rimuovere chiamata a `userService.getByToken()` e usare `SecurityContext`
  - Note: ⚠️ Richiede conferma prima di applicare modifiche. Modifica logica per usare sessioni.

---

## 5. File Analizzati

### Controller Admin

1. `src/main/java/com/funkard/adminauth/AdminAuthController.java`
2. `src/main/java/com/funkard/adminauth/AdminFixController.java`
3. `src/main/java/com/funkard/adminauth/AdminTokenController.java`
4. `src/main/java/com/funkard/adminauth/AccessRequestController.java`
5. `src/main/java/com/funkard/admin/AdminController.java`
6. `src/main/java/com/funkard/admin/controller/AdminDashboardController.java`
7. `src/main/java/com/funkard/admin/controller/AdminNotificationController.java`
8. `src/main/java/com/funkard/admin/controller/AdminNotificationBatchController.java`
9. `src/main/java/com/funkard/admin/controller/AdminNotificationArchiveController.java`
10. `src/main/java/com/funkard/admin/controller/AdminNotificationActionController.java`
11. `src/main/java/com/funkard/admin/controller/AdminNotificationStreamController.java`
12. `src/main/java/com/funkard/admin/controller/AdminNotificationCleanupController.java`
13. `src/main/java/com/funkard/admin/controller/AdminSupportController.java`
14. `src/main/java/com/funkard/admin/controller/AdminSupportCleanupController.java`
15. `src/main/java/com/funkard/admin/controller/AdminStatsController.java`
16. `src/main/java/com/funkard/admin/controller/AdminFranchiseController.java`
17. `src/main/java/com/funkard/admin/controller/FranchiseAdminController.java`
18. `src/main/java/com/funkard/admin/controller/AdminPendingValueController.java`
19. `src/main/java/com/funkard/admin/controller/AdminEmailLogController.java`
20. `src/main/java/com/funkard/admin/controller/CookieLogAdminController.java`
21. `src/main/java/com/funkard/admin/controller/EmailTemplateTestController.java`
22. `src/main/java/com/funkard/admin/controller/AdminValuationController.java`
23. `src/main/java/com/funkard/admin/controller/RolePermissionController.java`
24. `src/main/java/com/funkard/admin/controller/AdminLegacyAuthController.java`
25. `src/main/java/com/funkard/admin/controller/AdminCleanupController.java`
26. `src/main/java/com/funkard/admin/log/AdminActionLogController.java`
27. `src/main/java/com/funkard/admin/system/MaintenanceController.java`
28. `src/main/java/com/funkard/admin/system/SystemMaintenanceController.java`
29. `src/main/java/com/funkard/realtime/AdminStreamController.java`
30. `src/main/java/com/funkard/adminaccess/controller/AdminAccessController.java`
31. `src/main/java/com/funkard/controller/AdminTicketAssignmentController.java`
32. `src/main/java/com/funkard/controller/GradeLensAdminController.java`

### Config/Security

1. `src/main/java/com/funkard/config/SecurityConfig.java`
2. `src/main/java/com/funkard/adminauth/AdminSessionFilter.java`
3. `src/main/java/com/funkard/adminauth/AdminSessionService.java`

### Servizi Coinvolti

1. `src/main/java/com/funkard/adminauth/AdminUserService.java`
2. `src/main/java/com/funkard/adminauth/AdminUserRepository.java`
3. `src/main/java/com/funkard/admin/util/AdminAuthHelper.java`
4. `src/main/java/com/funkard/admin/AdminConfig.java`

---

## Riepilogo Statistiche

- **Totale endpoint analizzati**: 107
- **Endpoint completamente migrati (sessioni + @PreAuthorize)**: 51
- **Endpoint legacy (token statici/X-Admin-Token)**: 20
- **Endpoint pubblici (nessuna autenticazione)**: 25
- **Endpoint con dual auth (cron + @PreAuthorize)**: 6
- **Endpoint pubblici corretti (onboarding/login)**: 5

- **Endpoint CRITICI non protetti**: 16
- **Endpoint MEDI non protetti**: 9
- **Endpoint BASSO non protetti**: 0 (tutti i bassi sono o pubblici corretti o protetti)

---

**Data Analisi**: 2025-01-XX
**Versione Backend**: Analizzata su commit corrente
**Stato Migrazione**: ~48% completato (51/107 endpoint migrati)

