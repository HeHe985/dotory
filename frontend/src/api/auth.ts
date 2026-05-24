import { api } from "./client"

type ApiResponse<T> = {
  result: string
  data: T
  errorCode: string | null
  message: string | null
}

type LoginResponse = {
  accessToken: string
  refreshToken: string
}

export const signup = async (email: string, password: string, name: string) => {
  const res = await api.post<ApiResponse<string>>("/user/signup/user", {
    email,
    password,
    name,
  })

  return res.data.data
}

export const login = async (email: string, password: string) => {
  const res = await api.post<ApiResponse<LoginResponse>>("/auth/login", {
    email,
    password,
  })

  localStorage.setItem("accessToken", res.data.data.accessToken)
  localStorage.setItem("refreshToken", res.data.data.refreshToken)

  return res.data.data
}

export const logout = async () => {
  await api.post("/auth/logout")

  localStorage.removeItem("accessToken")
  localStorage.removeItem("refreshToken")
}

export const getKakaoLoginUrl = async () => {
  const res = await api.get<ApiResponse<string>>("/auth/kakao/login")
  return res.data.data
}

export const kakaoCallback = async (code: string) => {
  const res = await api.get<ApiResponse<LoginResponse>>(
    `/auth/kakao/callback?code=${code}`
  )

  localStorage.setItem("accessToken", res.data.data.accessToken)
  localStorage.setItem("refreshToken", res.data.data.refreshToken)

  return res.data.data
}