                    #Sistema de Gestão de Inventário

Aplicaç#ão de consola em Java para gerir lojas, produtos, vendas, utilizadores e relatórios de inventário.

## Funcionalidades

- Autenticação de utilizadores com registo e login por email ou telefone.
- Gestão de lojas: criar, listar, selecionar, ver detalhes e eliminar.
- Gestão de produtos por loja: adicionar, listar, consultar stock, repor stock, identificar stock baixo e eliminar.
- Registo de vendas com cálculo automático do total e redução de stock.
- Relatórios de vendas, inventário completo e resumo do sistema.
- Persistência de dados em ficheiros dentro da pasta `data/`.
- Criação de backup dos ficheiros principais de dados.
- Inserção de dados de teste para começar rapidamente.

## Requisitos

- Java Development Kit 24 ou superior para compilar e executar o projeto.
- Apache Ant, ou então o NetBeans com suporte ao projeto Ant.

## Como executar

### Opção 1: via terminal sem Ant

Se tiver o JDK 24 instalado, pode compilar e executar diretamente no terminal a partir da raiz do projeto:

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out sistemadegestaodeinventario.SistemaDeGestaoDeInventario
```

### Opção 2: via terminal com Ant

Na raiz do projeto, execute:

```bash
ant clean run
```

Se quiser apenas gerar o JAR:

```bash
ant clean jar
```

O executável principal do projeto é `sistemadegestaodeinventario.SistemaDeGestaoDeInventario`.

### Opção 3: via NetBeans

1. Abra o projeto no NetBeans.
2. Aguarde a indexação e compilação.
3. Use a opção Run/Executar do projeto.

## Como usar

Ao iniciar, o sistema mostra o menu de autenticação:

1. Fazer login.
2. Cadastrar novo utilizador.
0. Sair.

Depois de autenticado, o menu principal permite aceder a:

1. Gestão de Lojas.
2. Gestão de Produtos.
3. Registar Vendas.
4. Relatórios e Consultas.
5. Configuração do Sistema.
0. Sair.

Algumas ações dependem de selecionar primeiro uma loja ativa.

## Dados e persistência

O sistema lê e grava informação na pasta `data/`.

Arquivos principais:

- `data/usuarios.txt`
- `data/lojas.txt`
- `data/loja_<ID>_produtos.txt`

  ## Observações

- A aplicação usa codificação UTF-8.
- As credenciais podem ser email ou telefone com 9 dígitos.
- As senhas devem ter pelo menos 8 caracteres.
- `data/loja_<ID>_vendas.csv`

Exemplos já incluídos no repositório:
