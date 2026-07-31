import { api } from "../../lib/api";

export interface LoginRequestData {
  username: string;
  password: string;
}

export interface LoginResponseData {
  accessToken: string;
}

export const loginApi = async (
  data: LoginRequestData,
): Promise<LoginResponseData> => {
  const response = await api.post("/auth/login", data);
  return response.data.data;
};

export const logoutApi = async (): Promise<void> => {
  await api.post("/auth/logout");
};
