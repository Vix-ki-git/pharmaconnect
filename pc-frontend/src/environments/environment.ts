// Resolves the backend API base URL at runtime so the same build works locally
// and inside GitHub Codespaces without rebuilding or extra config.
//
// Local dev: window.location.host is "localhost:4200" -> backend at http://localhost:10000
// Codespaces: window.location.host is "<name>-4200.app.github.dev"
//             -> backend at https://<name>-10000.app.github.dev (same forwarding pattern, different port)
function deriveApiBaseUrl(): string {
  if (typeof window === 'undefined') return 'http://localhost:10000';

  const host = window.location.host;

  if (host.endsWith('.app.github.dev') && host.includes('-4200.')) {
    return `${window.location.protocol}//${host.replace('-4200.', '-10000.')}`;
  }

  return 'http://localhost:10000';
}

export const environment = {
  apiBaseUrl: deriveApiBaseUrl(),
};
