@echo off
set /p UserInputPath= write destino: 
echo procurando %UserInputPath%
cd %UserInputPath%
git add --all
set /p Commitmsg = Msg do commit: 
git commit -m "asdasds"
git push origin master