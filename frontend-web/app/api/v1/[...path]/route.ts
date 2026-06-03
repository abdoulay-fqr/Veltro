/**
 * Next.js catch-all proxy for all API Gateway calls.
 *
 * Why this exists: the access_token is stored in an httpOnly cookie
 * (unreadable by browser JS). All requests to the Spring Boot API Gateway
 * require Authorization: Bearer {token}. This server-side proxy reads the
 * httpOnly cookie and injects the header before forwarding to the gateway.
 *
 * Browser → POST /api/v1/courses (Next.js, server-side)
 *         → reads access_token cookie
 *         → POST http://localhost:8080/api/v1/courses + Authorization header
 *         → returns gateway response to browser
 */

import { NextRequest, NextResponse } from "next/server";

const GATEWAY_BASE = process.env.API_GATEWAY_URL || "http://localhost:8080";

async function proxyRequest(
  req: NextRequest,
  context: { params: Promise<{ path: string[] }> }
): Promise<NextResponse> {
  const { path } = await context.params;
  const accessToken = req.cookies.get("access_token")?.value;

  // Build the target URL, preserving query parameters
  const targetPath = path.join("/");
  const targetUrl = new URL(`${GATEWAY_BASE}/api/v1/${targetPath}`);
  req.nextUrl.searchParams.forEach((value, key) => {
    targetUrl.searchParams.set(key, value);
  });

  // Forward content-type; inject Authorization header if token is present
  const headers: Record<string, string> = {};
  const contentType = req.headers.get("Content-Type");
  if (contentType) headers["Content-Type"] = contentType;
  if (accessToken) headers["Authorization"] = `Bearer ${accessToken}`;

  // Forward the request body for non-GET requests
  let body: string | undefined;
  if (req.method !== "GET" && req.method !== "HEAD") {
    body = await req.text();
  }

  try {
    const gatewayRes = await fetch(targetUrl.toString(), {
      method: req.method,
      headers,
      body,
    });

    const responseText = await gatewayRes.text();
    return new NextResponse(responseText, {
      status: gatewayRes.status,
      headers: {
        "Content-Type":
          gatewayRes.headers.get("Content-Type") ?? "application/json",
      },
    });
  } catch {
    return NextResponse.json(
      { success: false, message: "Gateway unreachable. Please try again." },
      { status: 503 }
    );
  }
}

export const GET    = proxyRequest;
export const POST   = proxyRequest;
export const PUT    = proxyRequest;
export const DELETE = proxyRequest;
export const PATCH  = proxyRequest;
