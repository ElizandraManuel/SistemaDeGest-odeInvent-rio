# SistemaDeGest-odeInvent-rio

Sistema de Gestão de Inventário
Aplicação de console em Java para gerenciar lojas, produtos, vendas, usuários e relatórios de inventário.

                 Funcionalidades
Autenticação de utilizadores com registo e login por email ou telefone.
Gestão de lojas: criar, listar, selecionar, ver detalhes e excluir.
Gestão de produtos por loja: adicionar, listar, consultar estoque, reportar estoque, identificar estoque baixo e excluir.
Registro de vendas com cálculo automático do total e redução de estoque.
Relatórios de vendas, inventário completo e resumo do sistema.
Persistência de dados em arquivos dentro da pasta data/.
Criação de backup dos arquivos principais de dados.
Inserção de dados de teste para começar rapidamente.
                 Requisitos
Java Development Kit 24 ou superior para compilar e executar o projeto.
Apache Ant, ou então o NetBeans com suporte ao projeto Ant.
Como executar
Opção 1: via terminal sem Ant
Se você tiver o JDK 24 instalado, pode compilar e executar diretamente no terminal a partir da raiz do projeto:

mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out sistemadegestaodeinventario.SistemaDeGestaoDeInventario
Opção 2: via terminal com Ant
Na raiz do projeto, execute:

ant clean run
Se quiser apenas gerar o JAR:

ant clean jar
O seguinte principal do projeto é sistemadegestaodeinventario.SistemaDeGestaoDeInventario.

Opção 3: via NetBeans
Abra o projeto no NetBeans.
Aguarde a indexação e compilação.
Utilize a opção Executar/Executar o projeto.
Como usar
Ao iniciar, o sistema mostra o menu de autenticação:

Fazer login.
Cadastrar novo usuário.
Sair.
Depois de autenticado, o menu principal permite acessar:

Gestão de Lojas.
Gestão de Produtos.
Registrar Vendas.
Relatórios e Consultas.
Configuração do Sistema.
Sair.
Algumas ações dependem de selecionar primeiro uma loja ativa.

Dados e persistência
O sistema lê e grave informação na pasta data/.

Arquivos principais:

data/usuarios.txt


                      Estrutura do projeto
src/sistemadegestaodeinventario/- classe principal da aplicação.
src/sistemadegestaodeinventario/modelo/- entidades como Loja, Produto, Venda e Usuario.
src/sistemadegestaodeinventario/negocio/- regras de negócio e gestão do inventário.
src/sistemadegestaodeinventario/persistencia/- leitura e escrita dos arquivos.
src/sistemadegestaodeinventario/ui/- interface de console.


                     Observações
A aplicação usa UTF-8.
As credenciais podem ser email ou telefone com 9 dígitos.
As senhas devem ter pelo menos 8 caracteres.
data/lojas.txt
data/loja_<ID>_produtos.txt
data/loja_<ID>_vendas.csv
