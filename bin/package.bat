@echo off
echo.

REM =====================================================
REM 宠迹 - Maven 打包脚本（Windows）
REM 用法：
REM   package.bat dev    测试环境打包（默认）
REM   package.bat prod   生产环境打包
REM =====================================================

set PROFILE=%1
if "%PROFILE%"=="" set PROFILE=dev

echo [信息] 开始打包 [%PROFILE% 环境]
echo.

%~d0
cd %~dp0

cd ..
call mvn clean package -P%PROFILE% -Dmaven.test.skip=true

echo.
echo [信息] 打包完成！环境: %PROFILE%
pause
