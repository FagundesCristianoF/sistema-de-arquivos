# Sistema de Arquivo

Sistema Kotlin desenvolvido na matéria de **Sistemas Operacionais 2** pelo curso **Engenharia de Computação - Instituto Federal do Sul de Minas Gerais Campus Poços de Caldas**

Veja também a versão em inglês em `README.md`.

# Objetivo

Foi desenvolvido um sistema de arquivos para um Sistema Operacional. É simulado um sistema de armazenamento binário, visando abstrair a leitura em disco das informações e assim implementar comandos como **ls, mkdir, cat** entre outros de um Sistema Linux.

Para implementação, era livre a escolha da quantidade de bits para execução do sistema, analisando as limitações do sistema como: 
  * limite de espaço de armazenamento
  * tamanho máximo do nome do arquivos 
  * tamanho máximo de um arquivo


O sistema possui uma interface gráfica inspirada em um terminal de Linux. Nele é possível armazenar as últimas instruções e realizar todo o processo de escrita, leitura e deletar arquivos do sistema.

# Build e execução (Gradle)

```bash
./gradlew build
./gradlew run
```
