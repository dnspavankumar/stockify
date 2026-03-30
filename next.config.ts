import type { NextConfig } from "next";

const configuredBackendBaseUrl =
  process.env.BACKEND_BASE_URL?.trim() ||
  process.env.NEXT_PUBLIC_BACKEND_BASE_URL?.trim();

const normalizedBackendBaseUrl = configuredBackendBaseUrl?.replace(/\/+$/, "");

const nextConfig: NextConfig = {
  eslint: {
    ignoreDuringBuilds: true,
  },
  typescript: {
    ignoreBuildErrors: true,
  },
  async rewrites() {
    if (!normalizedBackendBaseUrl) {
      return [];
    }

    return [
      {
        source: "/api/:path*",
        destination: `${normalizedBackendBaseUrl}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
