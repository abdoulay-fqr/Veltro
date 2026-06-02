"use client";

import { useState } from "react";
import { nfcApi, SimulateScanResponse } from "@/lib/api/nfc";
import { CreditCard, Search, CheckCircle, XCircle } from "lucide-react";

export default function NfcSimulator() {
  const [cardUid, setCardUid] = useState("");
  const [result, setResult] = useState<SimulateScanResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleScan = async () => {
    if (!cardUid.trim()) return;
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const res = await nfcApi.simulateScan(cardUid.trim());
      setResult(res.data.data);
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        "Card not found or scan failed";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="rounded-xl border border-zinc-200 bg-white p-6 space-y-4">
      <div className="flex items-center gap-2">
        <CreditCard size={16} className="text-zinc-500" />
        <h3 className="text-sm font-semibold text-zinc-900">NFC Simulator</h3>
      </div>

      <p className="text-xs text-zinc-500">
        Enter a card UID (e.g. card001, card002) to simulate a physical NFC scan and look up the member.
      </p>

      <div className="flex gap-2">
        <input
          value={cardUid}
          onChange={(e) => setCardUid(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleScan()}
          placeholder="card001"
          className="flex-1 rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900"
        />
        <button
          onClick={handleScan}
          disabled={loading || !cardUid.trim()}
          className="flex items-center gap-2 rounded-lg bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50 transition-colors"
        >
          <Search size={14} />
          {loading ? "Scanning…" : "Scan"}
        </button>
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-lg border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-700">
          <XCircle size={14} />
          {error}
        </div>
      )}

      {result && (
        <div className="rounded-lg border border-green-100 bg-green-50 p-4 space-y-3">
          <div className="flex items-center gap-2 text-green-700">
            <CheckCircle size={14} />
            <span className="text-sm font-medium">Card found</span>
            <span
              className={`ml-auto text-xs font-medium px-2 py-0.5 rounded-full ${
                result.cardStatus === "ACTIVE"
                  ? "bg-green-200 text-green-800"
                  : "bg-red-200 text-red-800"
              }`}
            >
              Card: {result.cardStatus}
            </span>
          </div>

          <div className="flex items-center gap-3">
            {result.avatarUrl ? (
              <img
                src={`http://localhost:8082${result.avatarUrl}`}
                alt="avatar"
                className="w-10 h-10 rounded-full object-cover border border-green-200"
              />
            ) : (
              <div className="w-10 h-10 rounded-full bg-green-100 flex items-center justify-center text-green-600 font-medium">
                {result.firstname[0]}
              </div>
            )}
            <div>
              <p className="text-sm font-semibold text-zinc-900">
                {result.firstname} {result.lastname}
              </p>
              <p className="text-xs text-zinc-500">{result.phone ?? "No phone"}</p>
            </div>
            <span
              className={`ml-auto text-xs font-medium px-2 py-0.5 rounded-full ${
                result.accountStatus === "ACTIVE"
                  ? "bg-green-100 text-green-700"
                  : "bg-red-100 text-red-700"
              }`}
            >
              Account: {result.accountStatus}
            </span>
          </div>

          <dl className="grid grid-cols-2 gap-2 text-xs">
            <div>
              <dt className="text-zinc-500">Card UID</dt>
              <dd className="font-mono font-medium text-zinc-800">{result.cardUid}</dd>
            </div>
            <div>
              <dt className="text-zinc-500">Member ID</dt>
              <dd className="text-zinc-800">#{result.memberId}</dd>
            </div>
          </dl>
        </div>
      )}
    </div>
  );
}
