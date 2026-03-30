"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Header from "@/components/Header";
import { getCachedUser, getCurrentUser, hasStoredSession, searchStocks } from "@/lib/api/client";

const ProtectedAppShell = ({ children }: { children: React.ReactNode }) => {
  const router = useRouter();
  const [user, setUser] = useState<User | null>(() => getCachedUser());
  const [initialStocks, setInitialStocks] = useState<StockWithWatchlistStatus[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;

    const loadApp = async () => {
      if (!hasStoredSession()) {
        router.replace("/sign-in");
        return;
      }

      const sessionUser = await getCurrentUser();
      if (!active) return;

      if (!sessionUser) {
        router.replace("/sign-in");
        return;
      }

      setUser(sessionUser);
      const stocks = await searchStocks();
      if (!active) return;

      setInitialStocks(stocks);
      setLoading(false);
    };

    loadApp();

    return () => {
      active = false;
    };
  }, [router]);

  if (loading || !user) {
    return (
      <main className="min-h-screen text-gray-400">
        <div className="container py-10">Loading...</div>
      </main>
    );
  }

  return (
    <main className="min-h-screen text-gray-400">
      <Header user={user} initialStocks={initialStocks} />

      <div className="container py-10">{children}</div>
    </main>
  );
};

export default ProtectedAppShell;
