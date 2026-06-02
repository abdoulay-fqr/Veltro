import api from "@/lib/api";

export interface SimulateScanResponse {
  cardUid: string;
  cardStatus: "ACTIVE" | "INACTIVE";
  memberId: number;
  userId: number;
  firstname: string;
  lastname: string;
  phone: string | null;
  avatarUrl: string | null;
  accountStatus: string;
}

export const nfcApi = {
  simulateScan(cardUid: string) {
    return api.post<{ data: SimulateScanResponse }>("/nfc/simulate-scan", { cardUid });
  },

  activateCard(memberProfileId: number, cardUid: string) {
    return api.post("/nfc/activate", { memberProfileId, cardUid });
  },

  deactivateCard(memberProfileId: number, cardUid: string) {
    return api.post("/nfc/deactivate", { memberProfileId, cardUid });
  },
};
