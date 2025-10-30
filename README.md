# App-Fluxar 📱

Repositório para o desenvolvimento do **aplicativo Android** da Fluxar, escrito em **Kotlin**.  
O app consome dados das APIs do ecossistema Fluxar e é pensado para uso por estoquistas (especialmente de indústrias).

---

## Objetivo 🔍

Este projeto contém o código-fonte do aplicativo móvel Android (Kotlin).  
O app permite que usuários visualizem indicadores e alertas do Fluxar, além de interagir com funcionalidades voltadas ao monitoramento de estoque e análises.

---

## Principais funcionalidades 🚀

- Aplicativo nativo Android escrito em **Kotlin**  
- Conexão com as APIs do ecossistema Fluxar para leitura de dados e ações (consumo via HTTP)  
- Telas para login, gerenciamento de estoque, visualização de movimentações e alertas  
- Preparado para execução em emuladores e dispositivos Android
- Build e gerenciamento via **Gradle (Kotlin DSL)**

---

## Requisitos de desenvolvimento 🛠️

- **Android Studio** (versão compatível com os arquivos Gradle do projeto)  
- **JDK 11** (ou versão requerida pelo Gradle do projeto)  
- Android SDK (componentes para o `compileSdk` / `targetSdk` definidos no projeto)  
- Conexão com as APIs (variáveis de ambiente / arquivo `.env` conforme configuração do projeto)

---

## Desenvolvimento 🛠️
<p>
  <img src="./.github/images/Kotlin_Icon.png" alt="Kotlin" width="29"/>
  <img src="./.github/images/Android_Icon.png" alt="Android" width="29"/>
</p>

---

## Instalação e execução 👨‍💻

### 1️ - Clonar o repositório
```bash
git clone https://github.com/Fluxar-NeoTech/App-Fluxar.git
cd App-Fluxar
```

### 2️ - Abrir no Android Studio

* Abra o Android Studio → `Open` → selecione a pasta `App-Fluxar`
* Permita que o Android Studio sincronize o Gradle e baixe dependências

### 3️ - Configurar variáveis / secrets

Se o projeto usa arquivos de configuração locais (ex.: `local.properties`, `gradle.properties` com chaves, ou arquivos de recurso), crie-os conforme as instruções do time. Exemplo comum:

```
# local.properties (Android SDK path)
sdk.dir=/caminho/para/android/sdk
```

Se o app precisa de uma URL base da API, defina em arquivo de configuração ou constants (ex.: `BuildConfig` ou resource):

```
API_BASE_URL=https://api-fluxar.onrender.com
```

*(A localização exata dessas variáveis depende da implementação do projeto — verifique `app/src/main/java/.../config` ou `build.gradle.kts` caso exista referência explícita.)*

### 4️ - Build e execução

#### Executando via Android Studio

* Selecione um **emulador** ou um dispositivo físico conectado
* Clique em **Run** (▶) no Android Studio

#### Executando via linha de comando (Gradle wrapper)

```bash
./gradlew assembleDebug        
./gradlew installDebug         
```

---

## Estrutura do projeto 📐

```
App-Fluxar/
│  build.gradle.kts
│  settings.gradle.kts
│  gradle/...
│  gradlew, gradlew.bat
│
└─ app/
    ├─ src/
    │   ├─ main/
    │   │   ├─ java/                # Código Kotlin (pacotes)
    │   │   ├─ res/                 # layouts, drawables, strings
    │   │   └─ AndroidManifest.xml
    │   └─ test/
    └─ build.gradle.kts
```

---

## Dependências e plugins

O projeto usa **Gradle (Kotlin DSL)**. As dependências exatas estão declaradas em `app/build.gradle.kts` e no `build.gradle.kts` raiz. Abra esses arquivos para ver as versões usadas e bibliotecas incluídas.

---

## Testes e QA ✅

* Adicione/execute testes unitários e instrumentados (JUnit / AndroidX Test / Espresso), conforme a pasta `src/test` e `src/androidTest`
* Use emuladores (Android Studio AVD) e dispositivos reais para validação de fluxos e performance

---

## Licença 📝

Este projeto está licenciado sob a **licença MIT**. Consulte o arquivo [LICENSE](LICENSE) para mais detalhes.