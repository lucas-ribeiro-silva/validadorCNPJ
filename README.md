Markdown

# Validador de CNPJ (Java Swing)

![Java Swing](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-Java%20UI-blue?style=for-the-badge)
![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)

---

## 📄 Sobre o Projeto

Este é um projeto simples de aplicativo de desktop desenvolvido em **Java Swing** para **validar matematicamente números de CNPJ** e **consultar dados cadastrais de empresas** utilizando a **API pública da ReceitaWS**.

O objetivo principal é demonstrar a criação de uma interface gráfica básica (GUI) com Swing, a integração de APIs externas para consumo de dados (HTTP Requests e JSON Parsing), e o gerenciamento de dependências usando **Maven**.

### ✨ Funcionalidades

* **Validação Matemática de CNPJ**: Verifica a integridade do CNPJ baseado em seu algoritmo de cálculo de dígitos verificadores, antes mesmo de realizar consultas externas.
* **Consulta à API ReceitaWS**: Busca informações públicas de empresas (Razão Social, Nome Fantasia, Endereço completo, CEP, Bairro, Cidade/UF, Status na Receita Federal) a partir de um CNPJ válido.
* **Interface Gráfica Intuitiva**: Desenvolvida com Java Swing para uma experiência de usuário simples.
* **Gerenciamento de Dependências com Maven**: Utilização do Maven para gerenciar bibliotecas externas (`OkHttp` para requisições HTTP e `Gson` para parsing JSON).

---

## 🚀 Tecnologias Utilizadas

* **Java (JDK 23)**: Linguagem de programação.
* **Java Swing**: Toolkit para construção da interface gráfica.
* **Apache Maven**: Ferramenta de automação de build e gerenciamento de dependências.
* **OkHttp**: Cliente HTTP para requisições de rede.
* **Google Gson**: Biblioteca para serialização/desserialização de objetos Java para/de JSON.
* **ReceitaWS API**: API pública para consulta de dados de CNPJ.

---

## ⚙️ Como Configurar e Executar

Siga os passos abaixo para clonar o repositório, configurar o projeto e executá-lo em sua máquina.

### Pré-requisitos

Certifique-se de ter o seguinte instalado:

* **Java Development Kit (JDK) 23 ou superior**
* **Apache Maven 3.x**
* Uma IDE Java (como IntelliJ IDEA, Eclipse ou VS Code com extensões Java)

### ⬇️ Clonando o Repositório

## 📦 Configuração do Projeto (Maven)

O projeto utiliza Maven para gerenciar suas dependências.

### Importando o projeto na sua IDE:

- **IntelliJ IDEA**: Vá em *File > Open*, navegue até a pasta clonada e clique em *Open*. O IntelliJ deve reconhecer automaticamente o `pom.xml` e importar o projeto Maven.
- **Eclipse**: Vá em *File > Import... > Maven > Existing Maven Projects*, selecione a pasta raiz do projeto e clique em *Finish*.
- **VS Code**: Abra a pasta raiz do projeto. O VS Code com as extensões Java deve reconhecer o projeto Maven.

## ▶️ Executando o Aplicativo

Após a configuração, você pode executar o aplicativo diretamente da sua IDE:

1. Abra o arquivo `src/main/java/com/minhaempresa/validador/Main.java`.
2. Localize o método `main`.
3. Clique no ícone de *Play* (geralmente um triângulo verde) ao lado do método `main` e selecione *Run 'Main.main()'*.

O aplicativo de validação de CNPJ com interface gráfica deverá ser exibido.

---
## 📧 Contato
**Lucas Ribeiro Silva** - dev.lucasribeirosilva@gmail.com

- [LinkedIn](https://www.linkedin.com/in/dev-lucasribeirosilva/)
- [GitHub](https://github.com/lucas-ribeiro-silva)
