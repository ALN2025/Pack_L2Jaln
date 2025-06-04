# L2jALN - Lineage 2 Java Server Pack

[![Java](https://img.shields.io/badge/Java-17-blue?logo=java)]()
[![Status](https://img.shields.io/badge/status-Ativo-brightgreen)]()
[![UTF-8](https://img.shields.io/badge/Encoding-UTF8-blue)]()
[![Ant](https://img.shields.io/badge/Build-Ant-red)]()
[![L2J-aln](https://img.shields.io/badge/Projeto-L2J--aln-brightgreen)]()

---

## Sobre o Projeto

Sistema completo, moderno e otimizado para servidores privados de Lineage 2, desenvolvido em Java.
Baseado em aCis, oferece estabilidade, performance e recursos avançados para administradores e jogadores.

- **Logging Profissional**: Handlers customizados para logs de erro, item, chat, GM audit e mais.
- **Scripts otimizados**: Compatíveis com Windows e Linux, já configurados para o novo padrão de pacotes.
- **Build automatizado via Ant**: Geração de JAR completa e sem dependências externas.
- **Compatibilidade UTF-8**: Suporte a caracteres especiais e banners personalizados.
- **Estrutura modular**: Separação clara entre loginserver, gameserver, handlers, managers, eventos, scripts e sistemas de proteção.

---

## Tecnologias Utilizadas

- Java 17+
- Apache Ant
- UTF-8
- Shell Script & Batch Script
- Sistema de Logging Customizado

---

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

---

## Repositório

> **Privado** – uso exclusivo do autor
> GitHub: [ALN2025](https://github.com/ALN2025)

---

## Assinatura

```
Trabalho desenvolvido e pertencente a Anderson Luis do Nascimento  
Dev ⩿ A.L.N/⪀
```

---

L2jALN — O melhor da tecnologia Java para servidores Lineage 2! 