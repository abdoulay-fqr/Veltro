import { NextRequest, NextResponse } from "next/server";

export async function POST(req: NextRequest) {
  const accessToken = req.cookies.get("access_token")?.value;

  // Best-effort: blacklist token on backend
  if (accessToken) {
    try {
      const GATEWAY = process.env.API_GATEWAY_URL ?? "http://localhost:8080";
      await fetch(`${GATEWAY}/api/v1/auth/logout`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
      });
    } catch {
      // Don't block logout if backend is unreachable
    }
  }

  const response = NextResponse.json({ success: true });
  response.cookies.delete("access_token");
  response.cookies.delete("refresh_token");
  return response;
}