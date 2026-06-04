import { NextRequest, NextResponse } from "next/server";

function decodeJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const base64 = token.split(".")[1];
    const decoded = Buffer.from(base64, "base64url").toString("utf-8");
    return JSON.parse(decoded);
  } catch {
    return null;
  }
}

export async function GET(req: NextRequest) {
  const token = req.cookies.get("access_token")?.value;

  if (!token) {
    return NextResponse.json({ message: "Not authenticated" }, { status: 401 });
  }

  const payload = decodeJwtPayload(token);

  if (!payload) {
    return NextResponse.json({ message: "Invalid token" }, { status: 401 });
  }

  const exp = payload.exp as number | undefined;
  if (exp && Date.now() / 1000 > exp) {
    return NextResponse.json({ message: "Token expired" }, { status: 401 });
  }

  // userId is set as a custom JWT claim by the auth-service
  const rawUserId = payload.userId;
  const userId = typeof rawUserId === "number"
    ? rawUserId
    : typeof rawUserId === "string" ? parseInt(rawUserId, 10) || undefined : undefined;

  return NextResponse.json({
    identifier: payload.sub,
    role: payload.role,
    userId,
  });
}