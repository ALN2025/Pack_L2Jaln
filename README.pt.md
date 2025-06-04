# L2Jaln - Emulador de Servidor Lineage II

[![Licença](https://img.shields.io/badge/Licença-GPL%20v3-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-laranja.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Status](https://img.shields.io/badge/status-Ativo-verde.svg)]()
[![UTF-8](https://img.shields.io/badge/Encoding-UTF8-azul.svg)]()

## Visão Geral

L2Jaln é um emulador de servidor Lineage II construído sobre:
- Código fonte limpo do ACIS (source original limpa)
- Mods selecionados da comunidade L2J/Brasil
- Modificações desenvolvidas personalmente

Esta combinação fornece um ambiente de servidor Lineage II estável e rico em recursos.

## Características Principais

- **Base Limpa**: Construída sobre código fonte limpo do ACIS
- **Arquitetura Moderna**: Estrutura de pacotes atualizada e organização de código otimizada
- **Mods da Comunidade**: Melhorias selecionadas da comunidade L2J/Brasil
- **Recursos Personalizados**: Modificações e implementações próprias
- **Desempenho Aprimorado**: Otimização de desempenho e utilização de recursos
- **Documentação Abrangente**: Guias detalhados de configuração e instalação

## Sistema de Logging Profissional

- Handlers customizados para logs de erro
- Sistema de log de itens
- Log de chat e comandos
- Auditoria de ações GM
- Logs de PvP e eventos
- Sistema de debug avançado

## Requisitos Técnicos

- Java Development Kit (JDK) 17 ou superior
- Servidor MySQL 8.0 ou superior
- Mínimo de 4GB de RAM (8GB recomendado)
- Sistema operacional Windows/Linux
- Suporte a UTF-8 para caracteres especiais

## Estrutura do Projeto

```
L2Jaln/
├── java/                 # Implementação principal do servidor
│   ├── com.l2jaln/      # Pacote principal
│   │   ├── gameserver/  # Componentes do servidor de jogo
│   │   ├── loginserver/ # Componentes do servidor de login
│   │   └── util/        # Classes utilitárias
├── sql/                  # Scripts do banco de dados
├── config/              # Arquivos de configuração
└── tools/               # Ferramentas de gerenciamento do servidor
```

## Instalação

1. Clone o repositório
2. Importe o banco de dados usando os scripts SQL fornecidos
3. Configure as configurações do servidor em `config/l2jaln.ini`
4. Inicie o servidor de login e o servidor de jogo usando os scripts fornecidos

## Como Compilar e Executar

1. Compile o projeto:
   ```sh
   ant dist-local
   ```

2. Inicie os servidores (em terminais separados):
   ```sh
   cd "Pack L2Jaln/login"
   startLoginServer.bat
   ```
   ```sh
   cd "Pack L2Jaln/game"
   startGameServer.bat
   ```

> **Dica:** Use `chcp 65001` no terminal para melhor suporte a caracteres especiais.

## Configuração

O servidor pode ser configurado através dos seguintes arquivos:
- `config/l2jaln.ini`: Configuração principal do servidor
- `config/rates.properties`: Taxas e multiplicadores do jogo
- `config/network.properties`: Configurações de rede

## Contribuições da Comunidade

Este projeto foi enriquecido com várias modificações e melhorias da comunidade, incluindo:
- Sistema PvP aprimorado
- Eventos e recursos personalizados
- Correções de desempenho otimizadas
- Melhorias de segurança
- Correções de bugs e aprimoramentos de estabilidade

## Tecnologias Utilizadas

- Java 17+
- Apache Ant
- UTF-8
- Shell Script & Batch Script
- Sistema de Logging Customizado

## Licença

Este projeto está licenciado sob a GNU General Public License v3.0 - veja o arquivo [LICENSE](LICENSE) para detalhes.

## Agradecimentos

- Projeto ACIS por fornecer o código fonte limpo
- Comunidade L2J/Brasil pelos mods selecionados
- Agradecimento especial a Anderson Luis do Nascimento (A.L.N) por:
  - Desenvolvimento e adaptação completa
  - Modificações e melhorias personalizadas
  - Correções de bugs e otimizações
  - Manutenção e atualizações do sistema

> Nota: O código fonte limpo do ACIS usado como base para este projeto pode não estar disponível para download público.

## Sobre o Projeto

Sistema completo, moderno e otimizado para servidores privados de Lineage 2, desenvolvido em Java.
Baseado em aCis, oferece estabilidade, performance e recursos avançados para administradores e jogadores.

## Repositório

> **Privado** – uso exclusivo do autor
> GitHub: [ALN2025](https://github.com/ALN2025)

## Assinatura

```
Trabalho desenvolvido e pertencente a Anderson Luis do Nascimento  
Dev ⩿ A.L.N/⪀
```

---

L2jALN — O melhor da tecnologia Java para servidores Lineage 2! 