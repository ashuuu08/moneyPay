@echo off
echo Committing 1/5: AuthResponse Javadoc...
git add auth-service/src/main/java/com/moneyPay/auth/dto/AuthResponse.java
git commit -m "docs: add javadoc to AuthResponse DTO"

echo Committing 2/5: RegisterRequest Javadoc...
git add auth-service/src/main/java/com/moneyPay/auth/dto/RegisterRequest.java
git commit -m "docs: add javadoc to RegisterRequest DTO"

echo Committing 3/5: SecurityConfig Comments...
git add auth-service/src/main/java/com/moneyPay/auth/config/SecurityConfig.java
git commit -m "docs: add inline comment for security policies in SecurityConfig"

echo Committing 4/5: JwtService comments...
git add auth-service/src/main/java/com/moneyPay/auth/service/JwtService.java
git commit -m "docs: document extractUserId method in JwtService"

echo Committing 5/5: pom.xml description update...
git add payment-service/pom.xml
git commit -m "chore: update payment-service description in pom.xml"

echo All 5 commits have been created successfully!
pause
