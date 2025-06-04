# L2JALN - Lineage 2 Java Server Pack ✨
[![Java](https://img.shields.io/badge/Java-17+-blue)](https://www.java.com/)
[![Status](https://img.shields.io/badge/status-Ativo-green)](https://github.com/ALN2025/Pack_L2Jaln/actions)
[![Encoding](https://img.shields.io/badge/Encoding-UTF8-blue)](https://en.wikipedia.org/wiki/UTF-8)
[![Build](https://img.shields.io/badge/Build-Apache%20Ant-red)](https://ant.apache.org/)
[![License](https://img.shields.io/badge/License-GPLv3-blue)](https://www.gnu.org/licenses/gpl-3.0)
[![Project](https://img.shields.io/badge/Project-L2J--aln-orange)](https://github.com/ALN2025/Pack_L2Jaln)

L2Jaln é um servidor Lineage II baseado no aCis 382 (High Five), desenvolvido por Anderson Luis do Nascimento (A.L.N).

## ✨ Sobre o Projeto

L2Jaln é um servidor Lineage II moderno, baseado no aCis 382 (High Five), que oferece uma experiência de jogo completa e estável. O projeto inclui várias modificações personalizadas e melhorias em relação à base original.

### 🛡️ Características Principais

- Baseado em aCis 382 (High Five)
- Protocolo 746
- Sistema de proteção HWID
- Sistema de criptografia Blowfish
- Sistema de autenticação RSA
- Sistema de logging profissional
- Sistema de proteção contra flood
- Sistema de proteção de IP

### 🛠️ Recursos Implementados

- Sistema de Transformações
- Sistema de Vitality
- Sistema de Seven Signs
- Sistema de Subclasses
- Sistema de PvP
- Sistema de Eventos
- Sistema de Macros
- Sistema de Pesca
- Sistema de Clãs
- Sistema de Alianças
- Sistema de Academias
- Sistema de Rankings
- Sistema de Recomendações

## 💻 Requisitos do Sistema

### Servidor
- Java 8 ou superior
- MySQL 5.7 ou superior
- Mínimo 4GB RAM
- Processador Dual Core ou superior
- Sistema operacional: Windows/Linux

### Cliente
- Cliente Lineage II Interlude (Protocolo 730)
- Cliente Lineage II High Five (Protocolo 746)
- Suporte a UTF-8
- DirectX 9.0c ou superior

## 📥 Instalação

1. Clone o repositório:
```bash
git clone https://github.com/seu-usuario/L2Jaln.git
```

2. Configure o banco de dados:
- Crie um banco de dados MySQL
- Importe os arquivos SQL da pasta `sql/`
- Configure as credenciais em `config/database.properties`

3. Configure o servidor:
- Copie os arquivos de template da pasta `config/template/`
- Renomeie para `l2jaln.ini` e `loginserver.ini`
- Ajuste as configurações conforme necessário

4. Compile o projeto:
```bash
./gradlew build
```

5. Inicie o servidor:
```bash
# Login Server
./startLoginServer.bat

# Game Server
./startGameServer.bat
```

## 📚 Documentação

Para mais informações sobre a versão e configurações do servidor, consulte:
- [Documentação da Versão](SERVER_VERSION.md)
- [Guia de Configuração](docs/CONFIGURATION.md)
- [Guia de Instalação](docs/INSTALLATION.md)

## 🙋 Suporte

Para suporte técnico ou dúvidas sobre o projeto, entre em contato com:
- Desenvolvedor: Anderson Luis do Nascimento (A.L.N)
- Email: [Seu email]
- Discord: [Seu Discord]

## 📄 Licença

Este projeto é distribuído sob a licença [GPL-3.0](LICENSE).

## 🙏 Agradecimentos

- Equipe aCis pelo código fonte limpo
- Comunidade L2J/Brasil pelos mods selecionados
- Todos os contribuidores do projeto

---
*Desenvolvido por Anderson Luis do Nascimento (A.L.N)* 