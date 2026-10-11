# Herdeiros do Khan

Trabalho de INF1636 — Programação Orientada a Objetos, **1ª iteração**: componente Model em Java, com testes JUnit 4. O código-fonte é compatível com Java 8.

Esta versão implementa preparação, turnos, movimento, tributos, Khan, cidades, yurts, melhorias, conselheiros, habilidades dos herdeiros e pontuação. As escolhas iniciais são explícitas e obrigatórias antes de jogar. Apenas `Jogo` é pública; as demais classes do Model ficam ocultas no pacote.

## Estrutura

- `src/model/`: regras, estado do jogo e API pública.
- `test/model/`: suíte `TodosOsTestes` com **73 casos**, incluindo 18 regressões.
- `.project`, `.classpath` e `.settings/`: configuração do projeto Eclipse, mantida no repositório.
- [LEIAME.txt](LEIAME.txt): preparação pela API, regras, correções e limitações detalhadas.

## Abrir e testar no Eclipse

1. Clone ou baixe este repositório.
2. No Eclipse, use **File > Import > General > Existing Projects into Workspace > Select root directory** e selecione a pasta do projeto.
3. Configure um JDK na biblioteca JRE, caso necessário. O `.classpath` já referencia o JUnit 4 do Eclipse; não é necessário copiar JARs para o projeto.
4. Em `test/model/TodosOsTestes.java`, use **Run As > JUnit Test**.

A versão corrigida foi validada a partir de uma extração limpa do ZIP em Eclipse headless: compilação pelo JDT com **0 erros e 0 avisos**; JUnit com **73 testes executados e 0 falhas**.

## Testar pelo terminal

Em macOS ou Linux, com JDK 17 ou superior e as variáveis `JUNIT_JAR` e `HAMCREST_JAR` apontando para JUnit 4.13.2 e Hamcrest Core 1.3:

```sh
mkdir -p bin
javac --release 8 -encoding UTF-8 -cp "$JUNIT_JAR:$HAMCREST_JAR" -d bin src/model/*.java test/model/*.java
java -cp "bin:$JUNIT_JAR:$HAMCREST_JAR" org.junit.runner.JUnitCore model.TodosOsTestes
```

## Escopo e limitações

É uma entrega do Model da primeira iteração, sem interface gráfica. O mapa ainda usa ligações provisórias; a distribuição das cores das melhorias e algumas demandas dos conselheiros são suposições. O esgotamento das melhorias segue uma política documentada e testada. Consulte **Pendências e suposições** em [LEIAME.txt](LEIAME.txt) antes de considerar o projeto uma reprodução completa do jogo físico.
