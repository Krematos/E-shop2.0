/**
 * webhookService.js
 *
 * Centralizovaná správa webhooků.
 *
 * BEZPEČNOSTNÍ POZNÁMKA:
 * VITE_* proměnné jsou součástí klientského JS bundle a jsou tedy veřejně viditelné.
 * Pokud je to kritická URL (Discord webhook, Slack, atd.), doporučuje se dlouhodobě
 * volat webhook přes vlastní backend proxy endpoint místo přímo z frontendu.
 * Pro školní/interní projekt je toto řešení přijatelné.
 */

const WEBHOOK_URL = import.meta.env.VITE_WEBHOOK_URL ?? '';
const WEBHOOK_TIMEOUT_MS = Number(import.meta.env.VITE_WEBHOOK_TIMEOUT_MS ?? 5000);

/**
 * Odešle webhook notifikaci.
 *
 * @param {string} event - Název události (např. 'account_deleted')
 * @param {Record<string, unknown>} payload - Doplňující data události
 * @returns {Promise<void>}
 */
export const sendWebhook = async (event, payload = {}) => {
  if (!WEBHOOK_URL) {
    // Webhook není nakonfigurován – tiché přeskočení (dev prostředí)
    return;
  }

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), WEBHOOK_TIMEOUT_MS);

  try {
    const response = await fetch(WEBHOOK_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      signal: controller.signal,
      body: JSON.stringify({
        event,
        timestamp: new Date().toISOString(),
        ...payload,
      }),
    });

    if (!response.ok) {
      console.warn(`[webhookService] Webhook selhal se statusem ${response.status}`);
    }
  } catch (err) {
    if (err.name === 'AbortError') {
      console.warn('[webhookService] Webhook vypršel (timeout).');
    } else {
      console.warn('[webhookService] Webhook se nepodařilo odeslat:', err.message);
    }
    // Záměrně neháže výjimku – webhook neblokuje hlavní operaci
  } finally {
    clearTimeout(timeoutId);
  }
};
