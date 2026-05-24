import { useEffect, useState } from "react"
import {
  Alert,
  Button,
  SafeAreaView,
  Text,
  TextInput,
  View,
} from "react-native"
import * as WebBrowser from "expo-web-browser"
import { getKakaoLoginUrl, kakaoCallback, login, logout, signup } from "./src/api/auth"

WebBrowser.maybeCompleteAuthSession()

export default function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [mode, setMode] = useState<"login" | "signup">("login")

  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [name, setName] = useState("")

  useEffect(() => {
    const init = async () => {
      const url = new URL(window.location.href)
      const code = url.searchParams.get("code")

      if (code) {
        window.history.replaceState({}, document.title, "/")

        await kakaoCallback(code)

        setIsLoggedIn(true)
        return
      }

      const token = localStorage.getItem("accessToken")
      setIsLoggedIn(!!token)
    }

    init()
  }, [])

  const handleLogin = async () => {
    try {
      console.log("로그인 시도", email, password)

      const result = await login(email, password)

      console.log("로그인 성공", result)

      setIsLoggedIn(true)
    } catch (error: any) {
      console.log("로그인 실패 전체:", error)
      console.log("응답 데이터:", error.response?.data)
      console.log("상태 코드:", error.response?.status)

      Alert.alert(
        "로그인 실패",
        error.response?.data?.message ?? "이메일 또는 비밀번호를 확인해줘."
      )
    }
  }

  const handleSignup = async () => {
    try {
      await signup(email, password, name)
      Alert.alert("회원가입 완료", "이제 로그인해줘.")
      setMode("login")
    } catch {
      Alert.alert("회원가입 실패", "입력값을 확인해줘.")
    }
  }

  const handleKakaoLogin = async () => {
    try {
      const loginUrl = await getKakaoLoginUrl()

      window.location.href = loginUrl
    } catch (error) {
      console.log(error)
      Alert.alert("카카오 로그인 실패")
    }
  }

  const handleLogout = async () => {
    try {
      await logout()
      setIsLoggedIn(false)
    } catch {
      Alert.alert("로그아웃 실패")
    }
  }

  if (isLoggedIn) {
    return (
      <SafeAreaView style={{ flex: 1, padding: 24, justifyContent: "center" }}>
        <Text style={{ fontSize: 24, fontWeight: "bold", marginBottom: 20 }}>
          로그인 완료
        </Text>

        <Button title="로그아웃" onPress={handleLogout} />
      </SafeAreaView>
    )
  }

  return (
    <SafeAreaView style={{ flex: 1, padding: 24, justifyContent: "center" }}>
      <Text style={{ fontSize: 28, fontWeight: "bold", marginBottom: 24 }}>
        Dotory
      </Text>

      {mode === "signup" && (
        <TextInput
          placeholder="이름"
          value={name}
          onChangeText={setName}
          style={inputStyle}
        />
      )}

      <TextInput
        placeholder="이메일"
        value={email}
        onChangeText={setEmail}
        autoCapitalize="none"
        style={inputStyle}
      />

      <TextInput
        placeholder="비밀번호"
        value={password}
        onChangeText={setPassword}
        secureTextEntry
        style={inputStyle}
      />

      {mode === "login" ? (
        <>
          <Button title="로그인" onPress={handleLogin} />
          <View style={{ height: 12 }} />
          <Button title="카카오 로그인" onPress={handleKakaoLogin} />
          <View style={{ height: 12 }} />
          <Button title="회원가입으로 이동" onPress={() => setMode("signup")} />
        </>
      ) : (
        <>
          <Button title="회원가입" onPress={handleSignup} />
          <View style={{ height: 12 }} />
          <Button title="로그인으로 이동" onPress={() => setMode("login")} />
        </>
      )}
    </SafeAreaView>
  )
}

const inputStyle = {
  borderWidth: 1,
  borderColor: "#ddd",
  borderRadius: 8,
  padding: 12,
  marginBottom: 12,
}