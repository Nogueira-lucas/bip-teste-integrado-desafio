# frontend

Interface web Angular para gerenciamento de benefícios. Consome a API REST do `backend-module` e cobre todos os endpoints disponíveis: listar, buscar, criar, atualizar, inativar e transferência de saldo.

---

## Tecnologias

| Dependência | Versão | Finalidade |
|---|---|---|
| Angular | 20 (LTS) | Framework principal — standalone components |
| Angular Material | 20 | Componentes de UI (tabela, dialogs, snackbar, forms) |
| Angular CDK | 20 | Infraestrutura de UI (overlay, portal) |
| RxJS | 7.8 | Programação reativa / `Observable` |
| TypeScript | 5.9 | Linguagem |
| Zone.js | 0.15 | Detecção de mudanças |
| SCSS | — | Estilização |

### Dependências de desenvolvimento / teste

| Dependência | Versão | Finalidade |
|---|---|---|
| Jest | 29 | Runner de testes |
| jest-preset-angular | 14 | Integração Jest + Angular |
| ts-jest | 29 | Transpilação TypeScript para Jest |
| @types/jest | 29 | Tipos TypeScript para Jest |
| jest-environment-jsdom | 29 | Ambiente de DOM simulado |
| Angular CLI | 20 | Scaffolding, build e dev server |

---

## Estrutura do projeto

```
frontend/
├── src/
│   ├── main.ts                              # Ponto de entrada (bootstrapApplication)
│   ├── styles.scss                          # Estilos globais + tema Material
│   └── app/
│       ├── app.ts                           # Componente raiz (<router-outlet>)
│       ├── app.html                         # Template raiz
│       ├── app.config.ts                    # Providers globais (Router, HttpClient, Animations)
│       ├── app.routes.ts                    # Definição de rotas
│       ├── models/
│       │   └── beneficio.model.ts           # Interfaces Beneficio e TransferenciaRequest
│       ├── services/
│       │   └── beneficio.service.ts         # Chamadas HTTP para todos os endpoints
│       └── features/beneficios/
│           ├── beneficio-list/              # Tela principal: tabela + toolbar
│           ├── beneficio-form-dialog/       # Dialog criar / editar
│           └── transferencia-dialog/        # Dialog de transferência de saldo
├── proxy.conf.json                          # Proxy dev: /api → http://localhost:8080
├── jest.config.js                           # Configuração do Jest
├── setup-jest.ts                            # Inicialização do ambiente de testes
├── tsconfig.json                            # TypeScript base
├── tsconfig.app.json                        # TypeScript para build de produção
├── tsconfig.spec.json                       # TypeScript para testes (types: jest)
└── angular.json                             # Configuração do Angular CLI
```

---

## Pré-requisitos

- **Node.js** 18+ (recomendado 22.x)
- **npm** 9+
- **Backend rodando** em `http://localhost:8080` (ver [README raiz](../README.md#banco-de-dados))

---

## Instalação

```bash
cd frontend
npm install
```

---

## Executando em desenvolvimento

```bash
npm start
# equivalente: ng serve
```

A aplicação ficará disponível em `http://localhost:4200`.

O Angular CLI sobe um dev server com **proxy** configurado: todas as requisições para `/api/*` são redirecionadas para `http://localhost:8080`, evitando problemas de CORS.

> O backend e o banco de dados devem estar no ar antes de acessar a aplicação:
> ```bash
> # Na raiz do projeto
> docker compose up -d          # banco PostgreSQL
> java -jar backend-module/target/backend-module-0.0.1-SNAPSHOT.jar
> ```

---

## Build de produção

```bash
npm run build
# equivalente: ng build
```

Os artefatos são gerados em `dist/frontend/`. O build usa otimizações de produção (tree-shaking, minificação, hashing de nomes).

```bash
# Build com watch (desenvolvimento contínuo)
npm run watch
```

### Budgets configurados

| Tipo | Warning | Erro |
|---|---|---|
| Bundle inicial | 500 kB | 1 MB |
| Estilo por componente | 4 kB | 8 kB |

---

## Proxy de desenvolvimento

Arquivo: [proxy.conf.json](proxy.conf.json)

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  }
}
```

Toda requisição com prefixo `/api` é encaminhada ao backend durante o desenvolvimento. Em produção o servidor de hospedagem deve configurar o mesmo redirecionamento ou servir o frontend no mesmo host do backend.

---

## Rotas

| Caminho | Componente | Descrição |
|---|---|---|
| `/` | — | Redireciona para `/beneficios` |
| `/beneficios` | `BeneficioListComponent` | Listagem e gerenciamento de benefícios |

---

## Componentes e serviço

### `BeneficioService`

Localização: [src/app/services/beneficio.service.ts](src/app/services/beneficio.service.ts)

Mapeia todos os endpoints do backend:

| Método | HTTP | Endpoint |
|---|---|---|
| `listar()` | `GET` | `/api/v1/beneficios` |
| `buscar(id)` | `GET` | `/api/v1/beneficios/:id` |
| `criar(beneficio)` | `POST` | `/api/v1/beneficios` |
| `atualizar(id, beneficio)` | `PUT` | `/api/v1/beneficios/:id` |
| `deletar(id)` | `DELETE` | `/api/v1/beneficios/:id` |
| `transferir(req)` | `POST` | `/api/v1/beneficios/transferencia` |

> `transferir()` usa `responseType: 'text'` porque o backend retorna uma string simples em caso de sucesso. Em caso de erro, o corpo é um JSON `ProblemDetail` serializado como string — o `TransferenciaDialogComponent` faz o parse manualmente para extrair apenas o campo `detail`.

### `BeneficioListComponent`

Tela principal. Exibe uma `MatTable` com todos os benefícios ativos. Ações disponíveis na toolbar e por linha:

- **Novo** → abre `BeneficioFormDialogComponent` no modo criação
- **Editar** (ícone por linha) → abre `BeneficioFormDialogComponent` no modo edição
- **Inativar** (ícone por linha) → confirmação + `DELETE`
- **Transferência** → abre `TransferenciaDialogComponent`

### `BeneficioFormDialogComponent`

Dialog de formulário reativo para criar e editar benefícios.

| Campo | Validadores |
|---|---|
| `nome` | `required`, `maxLength(100)` |
| `descricao` | opcional |
| `valor` | `required`, `min(0.01)` |
| `ativo` | toggle (visível somente no modo edição) |

Detecta o modo pela presença de `data.id` injetado via `MAT_DIALOG_DATA`.

### `TransferenciaDialogComponent`

Dialog para transferir saldo entre dois benefícios.

| Campo | Validadores |
|---|---|
| `fromId` | `required` |
| `toId` | `required` |
| `valor` | `required`, `min(0.01)` |
| (grupo) | `diferentesValidator` — erro `mesmaConta` quando `fromId === toId` |

Tratamento de erro 500: extrai somente o campo `detail` do `ProblemDetail` retornado pelo backend e exibe no `MatSnackBar`.

---

## Testes

### Stack de testes

| Ferramenta | Papel |
|---|---|
| **Jest 29** | Runner, assertions, mocks (`jest.fn`, `jest.spyOn`) |
| **jest-preset-angular 14** | Integração com o compilador Angular |
| **TestBed** | Instanciação de componentes standalone |
| **HttpTestingController** | Interceptação e verificação de requisições HTTP |
| **NoopAnimationsModule** | Desabilita animações do Material nos testes |

### Executando os testes

```bash
# Todos os testes (uma vez)
npm test

# Modo watch — re-executa ao salvar
npx jest --watch

# Suite específica
npx jest beneficio.service
npx jest beneficio-form-dialog
npx jest transferencia-dialog
npx jest beneficio-list
npx jest app.routes
```

### Suites e cobertura de casos

| Suite | Arquivo | Testes | O que cobre |
|---|---|---|---|
| `BeneficioService` | `beneficio.service.spec.ts` | 8 | Todos os 6 métodos HTTP; verifica URL, método HTTP e corpo; confirma `responseType: 'text'` na transferência |
| `BeneficioFormDialog` | `beneficio-form-dialog.component.spec.ts` | 17 | Validadores `nome` e `valor`; modo criar vs. editar; `salvar()` chama `criar()`/`atualizar()`; erros com e sem `detail`; `cancelar()` |
| `TransferenciaDialog` | `transferencia-dialog.component.spec.ts` | 20 | Validadores de campo; `diferentesValidator` (mesma conta / contas distintas / campo nulo); erro com JSON string, objeto, sem `detail`, não-JSON e sem body |
| `BeneficioList` | `beneficio-list.component.spec.ts` | 18 | Carregamento inicial; erro ao listar; `abrirCriar`, `abrirEditar`, `inativar` (confirm true/false, erro), `abrirTransferencia`; recarga após fechar dialog |
| `app.routes` | `app.routes.spec.ts` | 3 | Redirect `'' → /beneficios`; componente da rota `/beneficios`; contagem de rotas |
| `App` | `app.spec.ts` | 2 | Instancia componente raiz; template contém `<router-outlet>` |

**Total: 68 testes em 6 suites**

---

## Cobertura de código

### Gerando o relatório

```bash
# Gera relatório e exibe resumo no terminal
npm run test:coverage
# equivalente: npx jest --coverage
```

O relatório HTML é gerado em `coverage/lcov-report/index.html`. Abra diretamente no navegador:

```bash
# Windows
start coverage/lcov-report/index.html

# macOS
open coverage/lcov-report/index.html

# Linux
xdg-open coverage/lcov-report/index.html
```

### Resultado atual

| Arquivo | Statements | Branches | Functions | Lines |
|---|---|---|---|---|
| `app.routes.ts` | 100% | 100% | 100% | 100% |
| `app.ts` | 100% | 100% | 100% | 100% |
| `beneficio.service.ts` | 100% | 100% | 100% | 100% |
| `beneficio-form-dialog.component.ts` | 100% | 100% | 100% | 100% |
| `beneficio-list.component.ts` | 100% | 100% | 100% | 100% |
| `transferencia-dialog.component.ts` | 100% | 100% | 100% | 100% |
| `app.config.ts` | 0%* | 100% | 100% | 0%* |
| **Total** | **95.86%** | **100%** | **100%** | **95.2%** |

> \* `app.config.ts` não é testado diretamente pois é puramente declarativo (registra providers globais). Seu conteúdo é exercitado indiretamente pelos testes de componentes e rotas, portanto a cobertura de 0% em statements/lines é esperada e aceitável.

### Interpretando as métricas

| Coluna | O que mede |
|---|---|
| **Statements** | Expressões individuais executadas |
| **Branches** | Desvios condicionais (`if`, `??`, `? :`, `&&`) cobertos |
| **Functions** | Funções e métodos invocados pelos testes |
| **Lines** | Linhas de código executadas ao menos uma vez |

---

## Nota técnica: override de providers em testes

Angular Material 20 registra serviços como `MatSnackBar` e `MatDialog` no environment injector do próprio standalone component (via módulos importados), com precedência sobre `TestBed.providers`. Para garantir que os mocks sejam injetados corretamente, os testes usam `TestBed.overrideProvider()` após `configureTestingModule`:

```ts
TestBed.configureTestingModule({ imports: [...], providers: [...] });
TestBed.overrideProvider(MatSnackBar, { useValue: snackSpy });
TestBed.overrideProvider(MatDialog,   { useValue: dialogSpy });
```

Essa abordagem garante que o mock substitua o provider em todos os níveis do injetor, independente de onde o serviço foi originalmente registrado.
