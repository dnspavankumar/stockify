"use client";

const BACKEND_BASE_URL =
  process.env.NEXT_PUBLIC_BACKEND_BASE_URL ?? "http://localhost:8080";

const SESSION_TOKEN_KEY = "signalist_session_token";
const SESSION_USER_KEY = "signalist_session_user";

type BackendErrorBody = {
  message?: string;
  error?: string;
};

type AuthResponse = {
  user: User;
  sessionToken: string;
  expiresAt: string;
};

const isBrowser = () => typeof window !== "undefined";

const getStoredToken = () => {
  if (!isBrowser()) return null;
  return window.localStorage.getItem(SESSION_TOKEN_KEY);
};

const getStoredUser = () => {
  if (!isBrowser()) return null;

  const raw = window.localStorage.getItem(SESSION_USER_KEY);
  if (!raw) return null;

  try {
    return JSON.parse(raw) as User;
  } catch {
    return null;
  }
};

const persistSession = ({ user, sessionToken }: AuthResponse) => {
  if (!isBrowser()) return;
  window.localStorage.setItem(SESSION_TOKEN_KEY, sessionToken);
  window.localStorage.setItem(SESSION_USER_KEY, JSON.stringify(user));
};

export const clearStoredSession = () => {
  if (!isBrowser()) return;
  window.localStorage.removeItem(SESSION_TOKEN_KEY);
  window.localStorage.removeItem(SESSION_USER_KEY);
};

export const hasStoredSession = () => Boolean(getStoredToken());

async function backendJson<T>(
  path: string,
  init: RequestInit = {},
  options: { includeSession?: boolean } = {}
): Promise<T> {
  const headers = new Headers(init.headers);
  headers.set("Accept", "application/json");

  if (init.body !== undefined && init.body !== null && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  if (options.includeSession !== false) {
    const token = getStoredToken();
    if (token) headers.set("X-Session-Token", token);
  }

  const response = await fetch(`${BACKEND_BASE_URL}${path}`, {
    ...init,
    headers,
  });

  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;

    try {
      const body = (await response.json()) as BackendErrorBody;
      message = body.message || body.error || message;
    } catch {
      const text = await response.text().catch(() => "");
      if (text) message = text;
    }

    if (response.status === 401) {
      clearStoredSession();
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export async function signUpWithEmail(
  payload: SignUpFormData
): Promise<{ success: boolean; data?: AuthResponse; error?: string }> {
  try {
    const response = await backendJson<AuthResponse>("/api/auth/sign-up", {
      method: "POST",
      body: JSON.stringify(payload),
    }, { includeSession: false });

    persistSession(response);
    return { success: true, data: response };
  } catch (error) {
    return {
      success: false,
      error: error instanceof Error ? error.message : "Sign up failed",
    };
  }
}

export async function signInWithEmail(
  payload: SignInFormData
): Promise<{ success: boolean; data?: AuthResponse; error?: string }> {
  try {
    const response = await backendJson<AuthResponse>("/api/auth/sign-in", {
      method: "POST",
      body: JSON.stringify(payload),
    }, { includeSession: false });

    persistSession(response);
    return { success: true, data: response };
  } catch (error) {
    return {
      success: false,
      error: error instanceof Error ? error.message : "Sign in failed",
    };
  }
}

export async function signOut() {
  try {
    await backendJson("/api/auth/sign-out", { method: "POST" });
  } catch {
    // Clear local session even if the backend no longer has the token.
  } finally {
    clearStoredSession();
  }
}

export async function getCurrentUser() {
  if (!getStoredToken()) return null;

  try {
    const response = await backendJson<{ user: User }>("/api/auth/session");
    if (isBrowser()) {
      window.localStorage.setItem(SESSION_USER_KEY, JSON.stringify(response.user));
    }
    return response.user;
  } catch {
    return null;
  }
}

export function getCachedUser() {
  return getStoredUser();
}

export async function searchStocks(query?: string): Promise<StockWithWatchlistStatus[]> {
  try {
    const trimmedQuery = typeof query === "string" ? query.trim() : "";
    const params = trimmedQuery ? `?query=${encodeURIComponent(trimmedQuery)}` : "";
    return await backendJson<StockWithWatchlistStatus[]>(`/api/stocks/search${params}`);
  } catch {
    return [];
  }
}

export async function getWatchlistStatus(symbol: string) {
  if (!getStoredToken()) return false;

  try {
    const response = await backendJson<{ symbol: string; isInWatchlist: boolean }>(
      `/api/watchlist/status?symbol=${encodeURIComponent(symbol.trim().toUpperCase())}`
    );
    return response.isInWatchlist;
  } catch {
    return false;
  }
}

export async function addToWatchlist(symbol: string, company: string) {
  return backendJson("/api/watchlist", {
    method: "POST",
    body: JSON.stringify({ symbol, company }),
  });
}

export async function removeFromWatchlist(symbol: string) {
  return backendJson(`/api/watchlist/${encodeURIComponent(symbol.trim().toUpperCase())}`, {
    method: "DELETE",
  });
}
