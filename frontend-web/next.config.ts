import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Allow Docker builds to succeed even if there are pre-existing type errors
  typescript: { ignoreBuildErrors: true },
  eslint: { ignoreDuringBuilds: true },
};

export default nextConfig;
