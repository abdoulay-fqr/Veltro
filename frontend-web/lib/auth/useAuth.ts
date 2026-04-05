"use client";

import { useContext } from "react";
import { AuthContext, AuthContextType } from "./AuthProvider";

export function useAuth(): AuthContextType {
  return useContext(AuthContext);
}