"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getCurrentUser, hasStoredSession } from "@/lib/api/client";

const GuestOnly = ({ children }: { children: React.ReactNode }) => {
  const router = useRouter();
  const [ready, setReady] = useState(false);

  useEffect(() => {
    let active = true;

    const checkSession = async () => {
      if (!hasStoredSession()) {
        if (active) setReady(true);
        return;
      }

      const user = await getCurrentUser();
      if (!active) return;

      if (user) {
        router.replace("/");
        return;
      }

      setReady(true);
    };

    checkSession();

    return () => {
      active = false;
    };
  }, [router]);

  if (!ready) return null;

  return <>{children}</>;
};

export default GuestOnly;
