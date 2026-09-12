<template>
  <div class="login-page">
    <!-- ============ 背景层：极光光晕 / 网格 / 光束 / 上浮粒子 ============ -->
    <div class="bg-layer" aria-hidden="true">
      <span class="orb orb-1"></span>
      <span class="orb orb-2"></span>
      <span class="orb orb-3"></span>
      <span class="orb orb-4"></span>
      <div class="grid-mask"></div>
      <div class="beam beam-a"></div>
      <div class="beam beam-b"></div>
      <span
        v-for="p in particles"
        :key="p.id"
        class="particle"
        :style="p.style"
      ></span>
    </div>

    <!-- ============ 登录卡片 ============ -->
    <div class="card-wrap">
      <div class="card-aura" aria-hidden="true"></div>

      <div class="login-card">
        <div class="card-body">
          <!-- 左侧：品牌区（窄屏隐藏） -->
          <aside class="brand-pane">
            <svg class="paw paw-1" viewBox="0 0 120 120" aria-hidden="true">
              <ellipse cx="60" cy="80" rx="30" ry="26" />
              <ellipse cx="26" cy="52" rx="11" ry="14" transform="rotate(-18 26 52)" />
              <ellipse cx="46" cy="33" rx="11" ry="15" transform="rotate(-8 46 33)" />
              <ellipse cx="74" cy="33" rx="11" ry="15" transform="rotate(8 74 33)" />
              <ellipse cx="94" cy="52" rx="11" ry="14" transform="rotate(18 94 52)" />
            </svg>
            <svg class="paw paw-2" viewBox="0 0 120 120" aria-hidden="true">
              <ellipse cx="60" cy="80" rx="30" ry="26" />
              <ellipse cx="26" cy="52" rx="11" ry="14" transform="rotate(-18 26 52)" />
              <ellipse cx="46" cy="33" rx="11" ry="15" transform="rotate(-8 46 33)" />
              <ellipse cx="74" cy="33" rx="11" ry="15" transform="rotate(8 74 33)" />
              <ellipse cx="94" cy="52" rx="11" ry="14" transform="rotate(18 94 52)" />
            </svg>

            <div class="brand-head">
              <div class="logo-shell">
                <span class="logo-halo"></span>
                <img src="@/assets/logo/logo.png" class="brand-logo" alt="宠迹" />
              </div>
              <h2 class="brand-name">{{ title }}</h2>
              <p class="brand-slogan">宠物档案 · 健康管理 · 积分商城</p>
            </div>

            <ul class="feature-list">
              <li v-for="item in features" :key="item.label" class="feature-item">
                <span class="feature-icon"><svg-icon :icon-class="item.icon" /></span>
                <span class="feature-label">{{ item.label }}</span>
              </li>
            </ul>
          </aside>

          <!-- 右侧：表单区 -->
          <section class="form-pane">
            <header class="form-head">
              <h3 class="title">欢迎回来</h3>
              <p class="subtitle">请使用管理员账号登录后台</p>
            </header>

            <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
              <el-form-item prop="username" class="field-user">
                <el-input
                  v-model="loginForm.username"
                  type="text"
                  auto-complete="off"
                  placeholder="请输入账号"
                  @keyup.enter.native="handleLogin"
                >
                  <svg-icon slot="prefix" icon-class="user" class="input-icon" />
                </el-input>
              </el-form-item>

              <el-form-item prop="password" class="field-pass">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  auto-complete="off"
                  placeholder="请输入密码"
                  @keyup.enter.native="handleLogin"
                >
                  <svg-icon slot="prefix" icon-class="password" class="input-icon" />
                </el-input>
              </el-form-item>

              <el-form-item prop="code" v-if="captchaEnabled" class="field-code">
                <div class="captcha-row">
                  <el-input
                    v-model="loginForm.code"
                    auto-complete="off"
                    placeholder="请输入验证码"
                    @keyup.enter.native="handleLogin"
                  >
                    <svg-icon slot="prefix" icon-class="validCode" class="input-icon" />
                  </el-input>
                  <div class="captcha-box" title="点击刷新验证码" @click="getCode">
                    <img :src="codeUrl" class="captcha-img" alt="验证码" />
                    <span class="captcha-mask">点击刷新</span>
                  </div>
                </div>
              </el-form-item>

              <div class="form-extra">
                <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
                <router-link v-if="register" class="link-type" :to="'/register'">立即注册</router-link>
              </div>

              <el-form-item class="field-submit">
                <el-button
                  :loading="loading"
                  type="primary"
                  class="login-btn"
                  @click.native.prevent="handleLogin"
                >
                  <span v-if="!loading">登 录</span>
                  <span v-else>登 录 中...</span>
                </el-button>
              </el-form-item>
            </el-form>
          </section>
        </div>
      </div>
    </div>

    <!-- 底部版权 -->
    <div v-if="footerContent" class="login-footer">{{ footerContent }}</div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from '@/utils/jsencrypt'
import defaultSettings from '@/settings'

// 粒子配色（品牌橙 + 冷调蓝紫，营造极光感）
const PARTICLE_COLORS = [
  "rgba(255,126,61,.85)",
  "rgba(255,178,94,.75)",
  "rgba(120,140,255,.7)",
  "rgba(255,255,255,.55)"
]

// 生成上浮粒子：位置、尺寸、时长、延迟均为一次性随机，避免运行期重排
function buildParticles(count) {
  const list = []
  for (let i = 0; i < count; i++) {
    const size = (Math.random() * 3 + 1.4).toFixed(1)
    list.push({
      id: i,
      style: {
        left: (Math.random() * 100).toFixed(2) + '%',
        width: size + 'px',
        height: size + 'px',
        backgroundColor: PARTICLE_COLORS[i % PARTICLE_COLORS.length],
        animationDuration: (Math.random() * 16 + 15).toFixed(1) + 's',
        animationDelay: '-' + (Math.random() * 32).toFixed(1) + 's'
      }
    })
  }
  return list
}

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      footerContent: defaultSettings.footerContent,
      codeUrl: "",
      // 左侧品牌区的能力点，图标取自 @/assets/icons/svg
      features: [
        { icon: 'form', label: '宠物档案与疫苗记录' },
        { icon: 'chart', label: '健康数据与体重趋势' },
        { icon: 'shopping', label: '积分商城与兑换订单' },
        { icon: 'peoples', label: '用户与权限统一管理' }
      ],
      particles: buildParticles(24),
      loginForm: {
        username: "",
        password: "",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      // 验证码开关
      captchaEnabled: true,
      // 注册开关
      register: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCode()
    this.getCookie()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/jpeg;base64," + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    getCookie() {
      const username = Cookies.get("username")
      const password = Cookies.get("password")
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 })
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove("username")
            Cookies.remove("password")
            Cookies.remove('rememberMe')
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{})
          }).catch(() => {
            this.loading = false
            if (this.captchaEnabled) {
              this.getCode()
            }
          })
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
$brand: #FF7E3D;
$brand-light: #FF9A4D;
$brand-amber: #FFB25E;
$ink-text: #F2F5FA;
$ink-muted: #7C879E;

/* ==================== 页面骨架 ==================== */
.login-page {
  position: relative;
  height: 100%;
  min-height: 100vh;
  padding: 24px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: radial-gradient(130% 110% at 50% -10%, #0C1430 0%, #060A16 46%, #03050C 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

/* ==================== 背景层 ==================== */
.bg-layer {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

/* 极光光晕（screen 混合保证叠加处提亮而非发灰） */
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(72px);
  mix-blend-mode: screen;
  will-change: transform;
}
.orb-1 {
  width: 660px; height: 660px; top: -250px; left: -190px;
  background: radial-gradient(circle, rgba(255,108,36,.95) 0%, rgba(255,108,36,.34) 42%, rgba(255,108,36,0) 70%);
  animation: orbDrift 20s ease-in-out infinite;
}
.orb-2 {
  width: 620px; height: 620px; bottom: -240px; right: -170px;
  background: radial-gradient(circle, rgba(84,82,255,.9) 0%, rgba(84,82,255,.3) 44%, rgba(84,82,255,0) 72%);
  animation: orbDrift2 26s ease-in-out infinite;
}
.orb-3 {
  width: 470px; height: 470px; top: 4%; right: 0;
  background: radial-gradient(circle, rgba(0,214,255,.68) 0%, rgba(0,214,255,.2) 44%, rgba(0,214,255,0) 72%);
  animation: orbDrift3 30s ease-in-out infinite;
}
.orb-4 {
  width: 440px; height: 440px; bottom: -6%; left: 2%;
  background: radial-gradient(circle, rgba(255,152,58,.78) 0%, rgba(255,152,58,.24) 44%, rgba(255,152,58,0) 72%);
  animation: orbDrift2 24s ease-in-out infinite reverse;
}

/* 点阵网格（中心向外淡出） */
.grid-mask {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(255,255,255,.14) 1px, transparent 1px);
  background-size: 36px 36px;
  -webkit-mask-image: radial-gradient(circle at 50% 48%, #000 0%, rgba(0,0,0,.35) 52%, transparent 78%);
  mask-image: radial-gradient(circle at 50% 48%, #000 0%, rgba(0,0,0,.35) 52%, transparent 78%);
  opacity: .7;
}

/* 四周压暗，突出中央卡片 */
.bg-layer::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: radial-gradient(115% 95% at 50% 50%, transparent 44%, rgba(0, 0, 0, .5) 100%);
}

/* 斜向光束 */
.beam {
  position: absolute;
  height: 1px;
  width: 55vw;
  left: -55vw;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,.45), transparent);
  will-change: transform;
}
.beam-a { top: 24%; animation: beamSweep 17s linear infinite; }
.beam-b { top: 70%; animation: beamSweep 23s linear infinite 5s; }

/* 上浮粒子 */
.particle {
  position: absolute;
  bottom: -12vh;
  border-radius: 50%;
  animation-name: particleRise;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
  box-shadow: 0 0 8px currentColor;
  will-change: transform, opacity;
}

@keyframes orbDrift {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50%      { transform: translate3d(90px, 70px, 0) scale(1.16); }
}
@keyframes orbDrift2 {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1.08); }
  50%      { transform: translate3d(-110px, -60px, 0) scale(.92); }
}
@keyframes orbDrift3 {
  0%, 100% { transform: translate3d(0, 0, 0) scale(.95); }
  50%      { transform: translate3d(-70px, 90px, 0) scale(1.2); }
}
@keyframes beamSweep {
  0%   { transform: rotate(-14deg) translateX(0);      opacity: 0; }
  12%  { opacity: .32; }
  88%  { opacity: .32; }
  100% { transform: rotate(-14deg) translateX(215vw);  opacity: 0; }
}
@keyframes particleRise {
  0%   { transform: translate3d(0, 0, 0) scale(.5);       opacity: 0; }
  12%  { opacity: 1; }
  78%  { opacity: .75; }
  100% { transform: translate3d(26px, -114vh, 0) scale(1); opacity: 0; }
}

/* ==================== 卡片外发光 ==================== */
.card-wrap {
  position: relative;
  width: 100%;
  max-width: 940px;
  z-index: 2;
  animation: cardIn .9s cubic-bezier(.16, 1, .3, 1) both;
}
.card-aura {
  position: absolute;
  inset: -60px -46px;
  z-index: -1;
  pointer-events: none;
  filter: blur(78px);
  opacity: .8;
  mix-blend-mode: screen;
  background:
    radial-gradient(50% 44% at 20% 26%, rgba(255,116,40,.44) 0%, rgba(255,116,40,0) 72%),
    radial-gradient(46% 46% at 86% 78%, rgba(84,82,255,.34) 0%, rgba(84,82,255,0) 72%);
}

/* ==================== 卡片本体（渐变描边 + 玻璃） ==================== */
.login-card {
  position: relative;
  padding: 1px;
  border-radius: 26px;
  background: linear-gradient(150deg,
    rgba(255,126,61,.6) 0%,
    rgba(255,255,255,.16) 30%,
    rgba(255,255,255,.06) 62%,
    rgba(96,112,255,.45) 100%);
  box-shadow:
    0 42px 90px -24px rgba(0, 0, 0, .78),
    0 0 70px -26px rgba(255, 126, 61, .5);
}
.card-body {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  min-height: 520px;
  border-radius: 25px;
  overflow: hidden;
  background: linear-gradient(160deg, rgba(23,29,48,.82) 0%, rgba(10,14,26,.88) 100%);
  -webkit-backdrop-filter: blur(30px) saturate(160%);
  backdrop-filter: blur(30px) saturate(160%);
}

/* ==================== 左侧品牌区 ==================== */
.brand-pane {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 34px;
  padding: 52px 44px;
  overflow: hidden;
  border-right: 1px solid rgba(255, 255, 255, .07);
  background: linear-gradient(160deg,
    rgba(255,126,61,.20) 0%,
    rgba(255,126,61,.06) 40%,
    rgba(255,126,61,0) 72%);
}
.paw {
  position: absolute;
  fill: #FFFFFF;
  pointer-events: none;
}
.paw-1 { width: 200px; height: 200px; right: -52px; bottom: -40px; opacity: .062; transform: rotate(-18deg); }
.paw-2 { width: 92px;  height: 92px;  left: -22px;  top: 10%;      opacity: .05;  transform: rotate(22deg); }

.brand-head {
  animation: fadeUp .8s cubic-bezier(.16, 1, .3, 1) both;
  animation-delay: .16s;
}
.logo-shell {
  position: relative;
  width: 92px;
  height: 92px;
  margin-bottom: 26px;
}
.logo-halo {
  position: absolute;
  inset: -14px;
  border-radius: 50%;
  filter: blur(13px);
  opacity: .62;
  animation: spinSlow 10s linear infinite;
  background: conic-gradient(from 0deg,
    rgba(255,126,61,.95),
    rgba(255,178,94,.12),
    rgba(96,112,255,.8),
    rgba(255,126,61,.95));
}
.brand-logo {
  position: relative;
  display: block;
  width: 92px;
  height: 92px;
  padding: 9px;
  box-sizing: border-box;
  border-radius: 28px;
  background: #FFFFFF;
  object-fit: contain;
  box-shadow: 0 18px 38px -14px rgba(255, 126, 61, .8);
}
.brand-name {
  margin: 0;
  font-size: 25px;
  font-weight: 600;
  letter-spacing: 1px;
  color: $ink-text;
}
.brand-slogan {
  margin: 10px 0 0;
  font-size: 13px;
  letter-spacing: .6px;
  color: rgba(255, 178, 94, .82);
}

.feature-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 13px;
  font-size: 13.5px;
  color: #B9C2D4;
  animation: fadeUp .8s cubic-bezier(.16, 1, .3, 1) both;
}
.feature-item:nth-child(1) { animation-delay: .30s; }
.feature-item:nth-child(2) { animation-delay: .37s; }
.feature-item:nth-child(3) { animation-delay: .44s; }
.feature-item:nth-child(4) { animation-delay: .51s; }

.feature-icon {
  flex: 0 0 30px;
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  font-size: 15px;
  color: #FFA05C;
  background: rgba(255, 126, 61, .14);
  border: 1px solid rgba(255, 126, 61, .24);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, .07);
}

/* ==================== 右侧表单区 ==================== */
.form-pane {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 50px 46px 44px;
}
.form-head .title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: .6px;
  color: $ink-text;
  animation: fadeUp .8s cubic-bezier(.16, 1, .3, 1) both;
  animation-delay: .12s;
}
.form-head .subtitle {
  margin: 9px 0 32px;
  font-size: 13px;
  color: $ink-muted;
  animation: fadeUp .8s cubic-bezier(.16, 1, .3, 1) both;
  animation-delay: .20s;
}

/* --- 输入框 --- */
::v-deep .login-form .el-input__inner {
  height: 46px;
  line-height: 46px;
  padding-left: 41px;
  border: 1px solid rgba(255, 255, 255, .1);
  border-radius: 12px;
  background: rgba(255, 255, 255, .05);
  color: #EDF1F8;
  font-size: 14px;
  transition: border-color .28s, background-color .28s, box-shadow .28s;
  &::placeholder { color: #5F6A80; }
  &:hover { border-color: rgba(255, 126, 61, .45); }
  &:focus {
    border-color: rgba(255, 126, 61, .9);
    background: rgba(255, 126, 61, .07);
    box-shadow: 0 0 0 3px rgba(255, 126, 61, .16);
  }
}
::v-deep .login-form .el-input__prefix {
  left: 14px;
  height: 100%;
  display: flex;
  align-items: center;
  color: #6C7791;
  transition: color .28s;
}
::v-deep .login-form .el-input:focus-within .el-input__prefix {
  color: $brand-light;
}
::v-deep .login-form .input-icon {
  width: 16px;
  height: 16px;
  font-size: 16px;
}
/* 浏览器自动填充时保持深色 */
::v-deep .login-form input:-webkit-autofill,
::v-deep .login-form input:-webkit-autofill:hover,
::v-deep .login-form input:-webkit-autofill:focus {
  -webkit-text-fill-color: #EDF1F8;
  -webkit-box-shadow: 0 0 0 1000px #161C2E inset;
  caret-color: #EDF1F8;
  transition: background-color 99999s ease-in-out 0s;
}

::v-deep .login-form .el-form-item { margin-bottom: 20px; }
::v-deep .login-form .el-form-item__error {
  padding-top: 3px;
  font-size: 12px;
  color: #FF7A7A;
}
::v-deep .login-form .el-form-item.is-error .el-input__inner {
  border-color: rgba(255, 122, 122, .8);
  background: rgba(255, 122, 122, .06);
}

/* --- 验证码 --- */
.captcha-row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}
::v-deep .captcha-row .el-input { flex: 1; }
.captcha-box {
  position: relative;
  flex: 0 0 132px;
  height: 46px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  background: #FFFFFF;
  border: 1px solid rgba(255, 255, 255, .1);
  transition: border-color .25s, box-shadow .25s;
}
.captcha-box:hover {
  border-color: rgba(255, 126, 61, .85);
  box-shadow: 0 0 0 3px rgba(255, 126, 61, .16);
}
.captcha-box:hover .captcha-mask { opacity: 1; }
.captcha-box:hover .captcha-img { transform: scale(1.05); }
.captcha-img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform .3s;
}
.captcha-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  letter-spacing: 1px;
  color: #FFB27A;
  background: rgba(10, 14, 26, .66);
  opacity: 0;
  transition: opacity .25s;
}

/* --- 记住密码 / 注册 --- */
.form-extra {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 2px 0 24px;
  animation: fadeUp .8s cubic-bezier(.16, 1, .3, 1) both;
  animation-delay: .44s;
}
::v-deep .login-form .el-checkbox { margin-right: 0; }
::v-deep .login-form .el-checkbox__label {
  padding-left: 9px;
  font-size: 13px;
  color: #8A93A8;
}
::v-deep .login-form .el-checkbox__inner {
  width: 16px;
  height: 16px;
  border-radius: 5px;
  border-color: rgba(255, 255, 255, .24);
  background: rgba(255, 255, 255, .06);
  transition: all .2s;
  &::after { left: 5px; top: 2px; }
}
::v-deep .login-form .el-checkbox__input.is-focus .el-checkbox__inner,
::v-deep .login-form .el-checkbox__input:hover .el-checkbox__inner {
  border-color: $brand;
}
::v-deep .login-form .el-checkbox__input.is-checked .el-checkbox__inner {
  border-color: $brand;
  background: linear-gradient(135deg, $brand-light, $brand);
  box-shadow: 0 0 10px rgba(255, 126, 61, .6);
}
::v-deep .login-form .el-checkbox__input.is-checked + .el-checkbox__label {
  color: #E6EAF2;
}
.link-type {
  font-size: 13px;
  color: $brand-amber;
  text-decoration: none;
  transition: color .2s, text-shadow .2s;
  &:hover { color: #FFD0A6; text-shadow: 0 0 12px rgba(255, 178, 94, .7); }
}

/* --- 登录按钮 --- */
::v-deep .login-btn {
  position: relative;
  width: 100%;
  height: 48px;
  overflow: hidden;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 4px;
  color: #FFFFFF;
  background: linear-gradient(120deg, $brand-light 0%, $brand 48%, #F0602A 100%);
  box-shadow: 0 16px 34px -14px rgba(255, 126, 61, .9), inset 0 1px 0 rgba(255, 255, 255, .28);
  transition: transform .22s, box-shadow .22s, filter .22s;
}
::v-deep .login-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: -120%;
  width: 55%;
  height: 100%;
  background: linear-gradient(100deg, transparent, rgba(255, 255, 255, .55), transparent);
  transform: skewX(-18deg);
  transition: left .75s cubic-bezier(.3, .7, .3, 1);
}
::v-deep .login-btn:hover {
  transform: translateY(-2px);
  filter: brightness(1.06);
  box-shadow: 0 22px 46px -16px rgba(255, 126, 61, 1), inset 0 1px 0 rgba(255, 255, 255, .3);
}
::v-deep .login-btn:hover::after { left: 130%; }
::v-deep .login-btn:active { transform: translateY(0) scale(.995); }
::v-deep .login-btn.is-loading { letter-spacing: 2px; }
::v-deep .login-form .field-submit { margin-bottom: 0; }

/* --- 入场节奏 --- */
::v-deep .login-form .field-user,
::v-deep .login-form .field-pass,
::v-deep .login-form .field-code,
::v-deep .login-form .field-submit {
  animation: fadeUp .8s cubic-bezier(.16, 1, .3, 1) both;
}
::v-deep .login-form .field-user   { animation-delay: .26s; }
::v-deep .login-form .field-pass   { animation-delay: .32s; }
::v-deep .login-form .field-code   { animation-delay: .38s; }
::v-deep .login-form .field-submit { animation-delay: .50s; }

/* ==================== 底部版权 ==================== */
.login-footer {
  position: absolute;
  left: 0;
  bottom: 18px;
  width: 100%;
  text-align: center;
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, .3);
  z-index: 3;
}

@keyframes cardIn {
  from { opacity: 0; transform: translate3d(0, 30px, 0) scale(.97); }
  to   { opacity: 1; transform: none; }
}
@keyframes fadeUp {
  from { opacity: 0; transform: translate3d(0, 14px, 0); }
  to   { opacity: 1; transform: none; }
}
@keyframes spinSlow {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

/* ==================== 响应式 ==================== */
@media (max-width: 980px) {
  .card-body { grid-template-columns: 1fr; min-height: auto; }
  .brand-pane { display: none; }
  .card-wrap { max-width: 440px; }
  .form-pane { padding: 42px 36px 36px; }
  .card-aura { inset: -50px -30px; }
}
@media (max-width: 480px) {
  .login-page { padding: 16px; }
  .card-wrap { max-width: 100%; }
  .form-pane { padding: 34px 24px 30px; }
  .captcha-box { flex: 0 0 112px; }
  .login-footer { bottom: 10px; }
}

/* 尊重系统「减少动态效果」偏好 */
@media (prefers-reduced-motion: reduce) {
  .orb,
  .beam,
  .particle,
  .card-wrap,
  .logo-halo,
  .brand-head,
  .feature-item,
  .form-head .title,
  .form-head .subtitle,
  .form-extra,
  ::v-deep .login-form .el-form-item {
    animation: none !important;
  }
}
</style>
