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

const PUBLIC_PATHS = ["/login", "/unauthorized", "/"];
const API_PATHS = ["/api/auth/login", "/api/auth/refresh", "/api/auth/logout", "/api/auth/me"];

const ROLE_ACCESS: Record<string, string[]> = {
  "/dashboard": ["ADMIN", "SUPER_ADMIN", "COACH"],
};

export function proxy(req: NextRequest) {
  const { pathname } = req.nextUrl;

  if (PUBLIC_PATHS.some((p) => p === "/" ? pathname === "/" : pathname.startsWith(p))) return NextResponse.next();
  if (API_PATHS.some((p) => pathname.startsWith(p))) return NextResponse.next();

  const token = req.cookies.get("access_token")?.value;

  if (!token) {
    return NextResponse.redirect(new URL("/login", req.url));
  }

  const payload = decodeJwtPayload(token);

  if (!payload) {
    return NextResponse.redirect(new URL("/login", req.url));
  }

  const exp = payload.exp as number | undefined;
  if (exp && Date.now() / 1000 > exp) {
    return NextResponse.redirect(new URL("/login", req.url));
  }

  const role = payload.role as string | undefined;

  for (const [route, allowedRoles] of Object.entries(ROLE_ACCESS)) {
    if (pathname.startsWith(route)) {
      if (!role || !allowedRoles.includes(role)) {
        return NextResponse.redirect(new URL("/unauthorized", req.url));
      }
    }
  }

  const requestHeaders = new Headers(req.headers);
  requestHeaders.set("x-user-role", role ?? "");
  requestHeaders.set("x-user-identifier", (payload.sub as string) ?? "");

  return NextResponse.next({ request: { headers: requestHeaders } });
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico|public/).*)"],
};