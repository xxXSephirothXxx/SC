Como utilizar(utilizando o eclipse oxigen):

1. Editar os ficheiros .policy no codeBase com o path da diretoria do projeto e colocar os ficheiros .policy dentro da diretoria.
2. Iniciar o PhotoShareServer indo a run configuration, adicionar 23232 como argumento e -Djava.security.manager -Djava.security.policy=server.policy em VM arguments.
3. Colocar as fotos que pretende usar na pasta da diretoria do projecto na maquina onde ira correr o cliente
4. Iniciar o PhotoShare indo a run confiruration, e adicionar como argumentos o nome de utizador,pwd e ip:porto ex:"miguel sporting123 10.101.148.120:23232" ou se preferir pode nao colocar a pwd, mas esta ira ser pedida atraves da linha de comandos assim que o programa iniciar. -Djava.security.manager -Djava.security.policy=client.policy em VM arguments.
5. Depois da ligacao estar estabelecida basta comecar a inserir os comandos descritos no enunciado na linha de comandos do cliente. por exemplo:
-a teste.jpg teste2.jpg
-f joaquim
-l joaquim
-c  estas muito bonito! joaquim teste.jpg

Nota: 
- O cliente pode correr apenas com a classe PhotoShare
- O programa so funciona com imagens com extensao .jpg e esta deve ser incluida no nome da foto como se verifica nos exemplos.
- Os programas estao feitos para rejeitar e lidar com qualquer tipo de comando mal formado
- Qualquer questao relacionada com os programas pode contactar os alunos 47886, 47859, 47895

