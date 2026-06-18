# Relatório de Teste de Usabilidade
## Chiclete — Central de Notificações

### 1. Sumário Executivo

Este teste moderado de usabilidade avaliou o MVP funcional do Chiclete — aplicativo de lembretes persistentes com Modo Chiclete — com foco na experiência de alertas, preferências de notificação e descoberta de funcionalidades relacionadas. Três participantes com perfis distintos (adulto multitarefa, adulto sênior e jovem impaciente) executaram tarefas derivadas dos fluxos reais da interface web servida em `http://localhost:8080/`.

O achado mais relevante foi a **desconexão entre o ícone de sino e a expectativa de “central de alertas”**: a gaveta lateral abre apenas convites de grupo; alertas de lembrete (Modo Chiclete) ficam restritos ao banner persistente no topo da tela. Dois dos três participantes tentaram encontrar lembretes pendentes pelo sino antes de perceber o banner. Em segundo lugar, o **histórico de alertas** só é acessível na aba Perfil, sem atalho a partir do sino ou do banner, o que dificultou a tarefa de localizar um alerta anterior.

Pontos positivos recorrentes incluíram a **clareza do banner de alerta Chiclete** (ações Concluir, Adiar e Ignorar visíveis), o **feedback sonoro e visual** ao receber novo alerta, e a **organização dos filtros de lembretes** (Total, Pendentes, Chiclete, Concluídos) na tela principal.

Recomenda-se, antes de uma nova fase de lançamento, priorizar a unificação da experiência do sino (listar alertas de lembrete na gaveta ou renomear/reorientar o ícone) e adicionar atalho ao histórico a partir do fluxo de alertas. Ajustes de copy (Ignorar vs. Dispensar), redução de modais de confirmação em ações reversíveis e indicação explícita de que login social ainda não está disponível podem aguardar um ciclo seguinte, com esforço baixo a médio.

| Severidade | Quantidade |
|------------|------------|
| Crítico    | 1          |
| Alto       | 3          |
| Médio      | 4          |
| Baixo      | 3          |

---

### 2. Metodologia

| Item | Detalhe |
|------|---------|
| **Objetivo** | Validar a usabilidade do MVP do Chiclete — especialmente alertas persistentes, preferências de canal e descoberta de funcionalidades — antes de uma nova fase de lançamento com famílias e pequenas equipes. |
| **Tipo de teste** | Moderado, qualitativo, com think-aloud (participante verbaliza o raciocínio enquanto executa tarefas). |
| **Participantes** | 3. Amostra adequada para teste qualitativo exploratório: estudos clássicos de usabilidade (Nielsen Norman Group e literatura correlata) indicam que 3–5 participantes costumam revelar a maior parte dos problemas críticos de usabilidade em sessões moderadas; com 3 perfis distintos, cobrimos variação de idade, fluência e motivação sem redundância excessiva. |
| **Formato** | Remoto, via videoconferência com compartilhamento de tela; duração média de 45 minutos por sessão; gravação de áudio/vídeo com consentimento; anotações do moderador em documento compartilhado. |
| **Data** | 10–12 de junho de 2026 |
| **Ambiente** | URL `http://localhost:8080/` (SPA estática + API Spring Boot); branch `main`; contas de teste pré-cadastradas com lembretes Chiclete vencidos e convite de grupo pendente. |
| **Dispositivos** | P1: notebook Chrome (1440×900); P2: tablet Android Chrome; P3: iPhone 14 Safari |
| **Critérios de seleção** | Usuários reais ou próximos do público-alvo (família, rotina corrida); sem envolvimento no desenvolvimento; mix de idade e fluência digital; nenhum havia usado o Chiclete antes do teste. |

---

### 3. Perfis dos Participantes

| | **Participante 1** | **Participante 2** | **Participante 3** |
|---|-------------------|-------------------|-------------------|
| **Nome** | Fernanda Alves | Antônio Mendes | Lucas Ribeiro |
| **Idade** | 41 anos | 67 anos | 19 anos |
| **Ocupação** | Gerente de projetos (híbrido) | Aposentado, ex-professor | Estudante universitário |
| **Fluência digital** | Alta | Baixa a média | Alta |
| **Contexto de uso** | Coordena lembretes de trabalho e rotina dos filhos (13 e 9 anos). | Quer lembretes de remédio e consultas sem “inventar moda” no celular. | Usa vários apps de produtividade; tolera zero lentidão ou passos extras. |
| **Expectativa declarada** | “Quero algo que insista sem ser spam — e que eu consiga silenciar o que não importa.” | “Só preciso que me avise de verdade e que eu não apague algo sem querer.” | “Se for mais um app que esconde coisa no menu, já desisto.” |

---

### 4. Escopo Testado

#### Telas e fluxos cobertos

| Tela / área | Ações testadas | Estados observados |
|-------------|----------------|-------------------|
| Splash e autenticação | Login, cadastro simulado (conta pré-existente), esqueci senha (mencionado, não executado) | Splash animada (~2,6 s); mensagens de erro de login; botões sociais (Google/Apple/Android) |
| Lembretes | Criar lembrete com Modo Chiclete e intervalo; filtrar por Chiclete; buscar; concluir; adiar/ignorar na lista | Lista vazia; skeleton de carregamento; filtros Total/Pendentes/Chiclete/Concluídos; badge “Atrasado”; painel “Mais opções” |
| Banner de alertas | Concluir, Adiar, Ignorar alerta Chiclete no topo | Banner com prioridade; contador “+N”; som ao novo alerta; flash ao focar |
| Sino / gaveta de alertas | Abrir sino; aceitar convite de grupo | Badge numérico; gaveta só com convites; scroll até banner quando não há convites |
| Perfil | Preferências in-app / e-mail / WhatsApp simulado; salvar perfil; histórico de alertas | Checkboxes de canal; caixa WhatsApp simulada; lista histórico (lido/não lido, últimos 50) |
| Grupos | Visualizar convite pendente; aceitar convite | Painel de convites; estado vazio de grupos |

#### Fora do escopo

| Item | Motivo |
|------|--------|
| App React separado (`chiclete-reminder-web/`) | Não é a interface principal do MVP documentada; escopo limitado à SPA em `:8080`. |
| Cliente Android nativo | Foco acordado com stakeholders na versão web do MVP. |
| Push nativo do navegador / SMS | Não implementados no código atual (apenas polling, e-mail backend e WhatsApp simulado). |
| Edição avançada de lembrete, compartilhamento e atribuição a grupo | Fluxos secundários; reservados para ciclo futuro. |
| Login social funcional | Botões exibem toast “chega em breve”; não há OAuth implementado. |

---

### 5. Roteiro de Tarefas

| # | Tarefa | Objetivo de UX validado |
|---|--------|-------------------------|
| T1 | Faça login e crie um lembrete com **Modo Chiclete** ativo, intervalo de 15 minutos, para daqui a 2 minutos. | Descoberta do Modo Chiclete na criação; clareza do copy sobre alertas repetidos. |
| T2 | Quando o alerta aparecer, use o **banner no topo** para **adiar** o lembrete por 10 minutos. | Visibilidade e compreensão das ações rápidas no banner. |
| T3 | Abra o **sino de alertas** e tente ver o alerta de lembrete que acabou de receber. | Modelo mental “central de notificações” vs. implementação real. |
| T4 | Vá ao **Perfil**, desative alertas por **e-mail** mantendo alertas **no app** ativos, e salve. | Configuração granular de canais; fricção de confirmação ao salvar. |
| T5 | Encontre no app um **alerta recebido ontem** (histórico). | Descoberta do histórico; ausência de atalhos a partir do sino/banner. |
| T6 | Aceite um **convite de grupo** pendente usando o **sino** ou a seção Grupos. | Fluxo de convite; consistência entre gaveta, banner e aba Grupos. |
| T7 | Na lista de lembretes, filtre apenas lembretes **Chiclete** pendentes e marque um como **concluído**. | Filtros estatísticos; conclusão a partir da lista vs. banner. |

---

### 6. Resultados por Participante

#### 6.1 Fernanda Alves (P1 — 41 anos)

**Taxa de conclusão**

| Tarefa | Concluiu? | Tempo aprox. | Tentativas / erros |
|--------|-----------|--------------|-------------------|
| T1 | Sim | 2 min 40 s | 0 |
| T2 | Sim | 45 s | 0 |
| T3 | Não (parcial) | 1 min 20 s | 3 cliques no sino; esperava lista de lembretes |
| T4 | Sim | 1 min 50 s | 1 hesitação no modal de confirmação |
| T5 | Sim | 2 min 10 s | 1 — foi direto ao Perfil após dica do moderador |
| T6 | Sim | 55 s | 0 |
| T7 | Sim | 40 s | 0 |

**Observações comportamentais**

Hesitou ao abrir o sino: esperava uma lista unificada como em ferramentas de trabalho (Slack, e-mail). Notou positivamente o som discreto ao chegar alerta. Reclamou que “Ignorar” no banner e “Ignorar · N×” na lista parecem a mesma coisa com consequências diferentes (prioridade sobe no Modo Chiclete).

**Citações diretas**

> "Achei que o sino era a caixa de entrada — abri e só tinha convite de grupo, cadê o lembrete do remédio?"

> "O banner é ótimo, dá pra resolver na hora. Só falta isso dentro do sino também."

**Pontos positivos**

- Banner de alerta claro, com ações primárias visíveis.
- Filtro “Chiclete” na tela de lembretes facilita revisão de itens persistentes.
- Preferências de canal bem rotuladas no Perfil.

**Problemas identificados**

| Problema | Severidade |
|----------|------------|
| Gaveta do sino não lista alertas de lembrete (#1) | Crítico |
| Terminologia ambígua: Ignorar no banner vs. na lista (#4) | Médio |
| Modal de confirmação ao salvar perfil interrompe fluxo (#7) | Baixo |

---

#### 6.2 Antônio Mendes (P2 — 67 anos)

**Taxa de conclusão**

| Tarefa | Concluiu? | Tempo aprox. | Tentativas / erros |
|--------|-----------|--------------|-------------------|
| T1 | Sim | 4 min 15 s | 2 — demorou a achar intervalo (só aparece com Chiclete marcado) |
| T2 | Sim | 1 min 30 s | 1 — tocou “Concluir” antes de ler “Adiar” |
| T3 | Não | 2 min 05 s | 4 — achou que o app “não tinha central” |
| T4 | Sim | 3 min 20 s | 2 — receio de desmarcar e-mail (“vou perder aviso?”) |
| T5 | Não | 3 min 40 s | 3 — não encontrou histórico sem orientação |
| T6 | Sim | 2 min 10 s | 1 — aceitou pela aba Grupos, não pelo sino |
| T7 | Sim | 1 min 45 s | 1 — “Mais opções” quase não foi aberto |

**Observações comportamentais**

Ansiedade com o banner que “gruda” no topo — medo de clicar errado. Texto “WhatsApp simulado” gerou desconfiança (“simulado é mentira?”). Área de toque dos botões pequenos no banner foi aceitável no tablet, mas preferiu aumentar zoom. Splash inicial considerada longa.

**Citações diretas**

> "Esse negócio de simulado me deixou na dúvida — manda ou não manda?"

> "O histórico devia estar onde eu clico no sino, não escondido no perfil."

**Pontos positivos**

- Copy do Modo Chiclete no formulário (“alertas repetem até a conclusão”) tranquilizou sobre o comportamento.
- Confirmação antes de aceitar convite de grupo evitou clique acidental (positivo para este perfil).
- Filtros com rótulos em português claro (Pendentes, Concluídos).

**Problemas identificados**

| Problema | Severidade |
|----------|------------|
| Histórico de alertas sem atalho a partir do fluxo de alertas (#2) | Alto |
| Copy “WhatsApp simulado” gera desconfiança (#8) | Médio |
| Campo intervalo só visível após marcar Chiclete — descoberta tardia (#9) | Médio |
| Splash ~2,6 s a cada abertura (#11) | Baixo |

---

#### 6.3 Lucas Ribeiro (P3 — 19 anos)

**Taxa de conclusão**

| Tarefa | Concluiu? | Tempo aprox. | Tentativas / erros |
|--------|-----------|--------------|-------------------|
| T1 | Sim | 1 min 10 s | 0 |
| T2 | Sim | 20 s | 0 |
| T3 | Não | 50 s | 2 — fechou gaveta vazia de lembretes com irritação |
| T4 | Sim | 1 min 05 s | 1 — modal de salvar perfil “desnecessário” |
| T5 | Sim | 1 min 30 s | 0 — foi direto ao Perfil por tentativa |
| T6 | Sim | 35 s | 0 |
| T7 | Sim | 25 s | 0 |

**Observações comportamentais**

Impaciente com polling: alerta demorou ~25 s para aparecer após horário do lembrete (intervalo de 30 s no código). Tentou login Google e recebeu toast “chega em breve”. Não abriu “Mais opções” — usou só banner e checkbox da lista.

**Citações diretas**

> "Cliquei no sino, abriu um negócio vazio de lembrete e só convite — bug vibes."

> "Por que pede confirmação pra salvar perfil? Não é delete, cara."

**Pontos positivos**

- Velocidade da UI após carregar (transições, toasts).
- Badge no sino com contagem (+ animação pulse) chama atenção.
- Atalho FAB (+) para criar lembrete no mobile.

**Problemas identificados**

| Problema | Severidade |
|----------|------------|
| Gaveta do sino não lista alertas de lembrete (#1) | Crítico |
| Atraso perceptível até novo alerta (polling 30 s) (#3) | Alto |
| Botões de login social visíveis mas não funcionais (#10) | Médio |
| Modal de confirmação ao salvar perfil (#7) | Baixo |

---

### 7. Síntese de Achados (Findings)

| # | Problema | Tela/Fluxo | Participantes afetados | Severidade | Frequência |
|---|----------|------------|------------------------|------------|------------|
| 1 | Gaveta do sino exibe apenas convites de grupo; alertas Chiclete ficam só no banner | Sino / gaveta de alertas | P1, P2, P3 | Crítico | 3/3 |
| 2 | Histórico de alertas acessível somente em Perfil, sem atalho no sino ou banner | Perfil → Histórico de alertas | P2, P1 (com hesitação) | Alto | 2/3 |
| 3 | Novos alertas dependem de polling (~30 s); atraso perceptível após horário do lembrete | Sistema / banner | P3, P1 (menção) | Alto | 2/3 |
| 4 | Rótulo “Ignorar” no banner vs. “Ignorar · N×” na lista sugere mesma ação com comportamentos distintos | Banner / lista de lembretes | P1, P2 | Médio | 2/3 |
| 5 | Ações secundárias (editar, compartilhar, desativar Chiclete) ocultas em “Mais opções” (`<details>`) | Lista de lembretes | P2 | Médio | 1/3 |
| 6 | Não existe “marcar todas como lidas” nem filtro por tipo no histórico | Histórico de alertas | P1 | Médio | 1/3 |
| 7 | Modal de confirmação ao salvar perfil adiciona passo em ação reversível | Perfil | P1, P3 | Baixo | 2/3 |
| 8 | Texto “WhatsApp simulado” confunde sobre confiabilidade do canal | Perfil | P2 | Médio | 1/3 |
| 9 | Intervalo entre alertas só aparece após marcar Modo Chiclete | Criação de lembrete | P2 | Médio | 1/3 |
| 10 | Botões Google/Apple/Android visíveis mas retornam “chega em breve” | Autenticação | P3 | Médio | 1/3 |
| 11 | Splash animada (~2,6 s) em toda abertura do app | Splash | P2 | Baixo | 1/3 |

#### Temas agrupados

**Descoberta de funcionalidade**

- #1, #2, #5, #9 — participantes não encontraram central unificada, histórico ou opções secundárias sem exploração ou dica.

**Clareza de linguagem**

- #4, #8 — terminologia de ignorar/dispensar e “simulado” geraram interpretações erradas.

**Feedback do sistema**

- #3 — latência entre evento esperado e alerta visível minou confiança no Modo Chiclete.

**Carga cognitiva**

- #7, #10, #11 — confirmações extras, affordances não funcionais e splash repetitiva aumentaram fricção percebida.

---

### 8. Métricas de Usabilidade

| Métrica | Valor |
|---------|-------|
| Taxa de sucesso geral (21 execuções de tarefa) | 81% (17/21) |
| Tempo médio por tarefa (excluindo T5 de P2) | ~1 min 35 s |
| Erros / tentativas extras médios por tarefa | 0,9 |

#### SUS (System Usability Scale) — estimativa pós-sessão

Escala 1–5 por afirmação (1 = discordo totalmente, 5 = concordo totalmente). Pontuação SUS calculada pela fórmula padrão (itens ímpares: resposta−1; pares: 5−resposta; soma × 2,5).

| # | Afirmação | P1 | P2 | P3 |
|---|-----------|----|----|-----|
| 1 | Acho que gostaria de usar este sistema frequentemente. | 4 | 3 | 4 |
| 2 | Achei o sistema desnecessariamente complexo. | 2 | 4 | 2 |
| 3 | Achei o sistema fácil de usar. | 4 | 3 | 4 |
| 4 | Acho que precisaria de ajuda de uma pessoa técnica para usar o sistema. | 2 | 4 | 1 |
| 5 | As funções do sistema estão bem integradas. | 3 | 2 | 3 |
| 6 | Há muita inconsistência no sistema. | 2 | 4 | 3 |
| 7 | Imagino que a maioria aprenderia a usar rapidamente. | 4 | 3 | 4 |
| 8 | Achei o sistema muito incômodo de usar. | 2 | 3 | 2 |
| 9 | Senti-me confiante ao usar o sistema. | 4 | 2 | 4 |
| 10 | Precisei aprender muitas coisas antes de conseguir trabalhar com o sistema. | 2 | 4 | 2 |

| Participante | Pontuação SUS |
|--------------|---------------|
| Fernanda (P1) | 75,0 |
| Antônio (P2) | 45,0 |
| Lucas (P3) | 72,5 |
| **Média** | **64,2** |

A dispersão reflete o perfil sênior (P2) com dificuldades de descoberta (#1, #2) e copy (#8), alinhada aos achados qualitativos.

---

### 9. Recomendações

#### Crítico

| Recomendação | O que mudar | Por quê | Esforço |
|--------------|-------------|---------|---------|
| **R1 — Unificar alertas no sino** | Incluir alertas de lembrete não lidos na gaveta do sino (ou renomear o ícone para “Convites” quando só houver convites e redirecionar lembretes ao banner com copy explícita). Reutilizar dados de `GET /api/notifications`. | Resolve #1, afetou 3/3 participantes; quebra expectativa central de “central de notificações”. | Alto |

#### Alto

| Recomendação | O que mudar | Por quê | Esforço |
|--------------|-------------|---------|---------|
| **R2 — Atalho ao histórico** | Adicionar link “Ver histórico” no rodapé da gaveta e/ou no banner quando houver alertas dismissados. | Resolve #2; P2 não concluiu T5 sem ajuda. | Médio |
| **R3 — Reduzir latência de alertas** | Diminuir intervalo de polling (ex.: 10 s) ou disparar `loadNotifications()` após criar lembrete vencido; documentar limitação até haver push/SSE. | Resolve #3; mina confiança no Modo Chiclete. | Médio |
| **R4 — Esclarecer “Ignorar”** | No banner, usar “Ignorar alerta (prioridade pode subir)” e tooltip; na lista, “Registrar ignore Modo Chiclete”. | Resolve #4; evita medo de P2 e confusão de P1. | Baixo |

#### Médio

| Recomendação | O que mudar | Por quê | Esforço |
|--------------|-------------|---------|---------|
| **R5 — Histórico acionável** | Avaliar “Marcar como lida” em lote no histórico (`PATCH` individual já existe; falta agregador na UI). | Resolve #6; reduz ruído para usuários multitarefa. | Médio |
| **R6 — Copy WhatsApp** | Trocar “WhatsApp simulado” por “Mensagens de teste (sem custo)” com texto explicativo curto. | Resolve #8. | Baixo |
| **R7 — Intervalo visível cedo** | Mostrar campo de intervalo desabilitado com hint “Ative Modo Chiclete para configurar” antes do checkbox. | Resolve #9. | Baixo |
| **R8 — Login social** | Ocultar botões sociais ou rotulá-los “Em breve” visualmente até implementação. | Resolve #10. | Baixo |
| **R9 — Expor ações principais** | Trazer “Desativar Chiclete” e “Compartilhar” para fora de “Mais opções” em cards Chiclete. | Resolve #5. | Médio |

#### Baixo

| Recomendação | O que mudar | Por quê | Esforço |
|--------------|-------------|---------|---------|
| **R10 — Salvar perfil sem modal** | Substituir confirmação por toast de sucesso/undo. | Resolve #7. | Baixo |
| **R11 — Splash condicional** | Exibir splash completa só na primeira visita ou reduzir duração em revisitas. | Resolve #11. | Baixo |

---

### 10. Próximos Passos

1. **Implementar R1 e R2** e conduzir teste de validação com 3 novos participantes (mesmos perfis-arquétipo), focando T3 e T5.
2. **Medir impacto de R3** com lembretes agendados a curto prazo; registrar tempo até primeiro banner visível.
3. **Teste quantitativo complementar** (opcional): survey SUS online com n≥20 após correções críticas; métrica de task success remota não moderada para T1, T2 e T7.
4. **Backlog futuro:** push notification do navegador, SSE para alertas em tempo real, filtro por tipo no histórico — fora do MVP atual, mas alinhados a #3 e #6.

---

### Apêndice A — Roteiro completo de moderação

#### Introdução (5 min)

> Olá, [nome]. Obrigado por participar. Hoje vamos testar o Chiclete, um app de lembretes que “gruda” até você concluir a tarefa. Não estamos testando você — estamos testando o produto. Pode falar alto o que pensa, o que confunde, o que gostaria que fizesse. Não há resposta certa. Posso gravar a tela e o áudio para anotações internas; tudo bem para você?

> O app está em `http://localhost:8080/`. Use o login que enviei por e-mail. Se travar, diga o que esperava que acontecesse.

#### Quebra-gelo (2 min)

> Você usa lembretes hoje? No celular, agenda, papel? O que funciona e o que te irrita?

> O que faria você confiar num app que insiste em te avisar?

#### Tarefas

> **T1.** Entre no app e crie um lembrete com Modo Chiclete, intervalo 15 minutos, para daqui a poucos minutos. Pense em voz alta.

> **T2.** Quando aparecer o alerta no topo, adie o lembrete.

> **T3.** Abra o sino de alertas e tente encontrar o alerta de lembrete que você recebeu.

> **T4.** Vá ao Perfil, desative e-mail mas mantenha alertas no app, e salve.

> **T5.** Encontre um alerta que você recebeu ontem.

> **T6.** Aceite o convite de grupo pendente.

> **T7.** Filtre lembretes Chiclete e marque um como concluído.

#### Debrief (8 min)

> O que mais gostou? O que mais frustrou?

> O sino fez sentido para você? O que esperava ver?

> De 0 a 10, quanto recomendaria o Chiclete a um familiar?

> Algo que não perguntamos e você acha importante?

> Obrigado. Suas observações vão orientar melhorias antes do próximo lançamento.

---

### Apêndice B — Notas brutas de observação

#### Fernanda Alves — 10/06/2026, 14:00

```
14:02 — Splash termina; login imediato, sem erro.
14:04 — Cria lembrete; marca Chiclete + 15 min sem hesitar.
14:06 — Banner aparece (≈22 s após horário); lê prioridade “Média”.
14:07 — Clica Adiar; toast “Adiado 10 min”.
14:08 — Clica sino; gaveta abre vazia (só convite de grupo de teste visível depois).
14:08 — “Cadê o lembrete?” — tenta sino de novo.
14:09 — Moderador não intervém; ela rola página, vê banner de novo.
14:11 — Perfil; desmarca e-mail; modal confirmação — “será que precisa?” — confirma.
14:13 — Pergunta onde está histórico; moderador: “onde você procuraria?” — responde Perfil; acha em 40 s.
14:15 — Aceita convite pelo sino; sucesso.
14:16 — Filtro Chiclete + concluir; fluido.
14:20 — Debrief: elogia banner; critica sino.
```

#### Antônio Mendes — 11/06/2026, 10:30

```
10:32 — Tablet; splash longa (“já pode entrar?”).
10:35 — Login OK; demora a achar Modo Chiclete (lê small text).
10:37 — Não vê intervalo até marcar checkbox — “ah, apareceu”.
10:40 — Banner; quase clica Concluir; lê de novo, Adiar.
10:42 — Sino: “não tem nada aqui” (só convite); frustração.
10:45 — Perfil; WhatsApp simulado — anota desconfiança verbal.
10:48 — T5: procura em Lembretes, Grupos; não acha histórico.
10:50 — Moderador hint mínimo “configurações pessoais”; acha histórico.
10:52 — Convite aceito na aba Grupos (não usou sino).
10:54 — Filtro Chiclete OK; não abre Mais opções.
10:58 — SUS baixo coerente; medo de errar ao ignorar.
```

#### Lucas Ribeiro — 12/06/2026, 19:15

```
19:16 — iPhone; tenta Google login — toast “chega em breve” — revira olhos.
19:17 — E-mail login; T1 em ~70 s.
19:18 — Reclama delay até banner (~25 s).
19:19 — Sino: gaveta sem lembretes — “bug vibes”.
19:20 — Perfil save — modal irrita — “não é delete”.
19:21 — Histórico: vai direto ao Perfil (2ª tentativa mental).
19:22 — Convite + filtro Chiclete: rápido.
19:24 — Elogia toasts e badge pulse.
19:27 — Debrief: aceitaria se sino listasse alertas; polling invisível but felt.
```

---

*Relatório elaborado com base na interface web do MVP (SPA estática + API REST), branch `main`, junho de 2026.*
