# 🎮 L2Jaln - Lineage 2 Java Server Pack

[![Version](https://img.shields.io/badge/version-1.0-blue.svg)](https://github.com/ALN2025/pack_L2JALN)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)

> **Uma pack completa e profissional para servidores Lineage 2 Java com sistemas avançados de proteção, eventos automáticos e funcionalidades customizadas.**

---

## 📋 Índice

- [Características Principais](#-características-principais)
- [Sistemas de Proteção](#-sistemas-de-proteção)
- [Eventos Automáticos](#-eventos-automáticos)
- [Sistemas Customizados](#-sistemas-customizados)
- [Configurações Avançadas](#-configurações-avançadas)
- [Instalação](#-instalação)
- [Configuração](#-configuração)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Suporte](#-suporte)

---

## ⭐ Características Principais

### 🛡️ **Sistemas de Proteção Avançados**
- **Sistema HWID** - Proteção contra multi-boxing
- **Anti-Bot** - Sistema de captcha e validação
- **Proteção de Itens** - Restrições de venda, drop e trade
- **Proteção de Zonas** - Controle de acesso por HWID/IP
- **Anti-Zerg** - Prevenção de ressurreição em massa

### 🎯 **Eventos Automáticos**
- **Tournament** - Sistema completo de arena 1x1, 2x2, 5x5, 9x9
- **TvT (Team vs Team)** - Eventos de clã vs clã
- **CTF (Capture The Flag)** - Eventos de captura de bandeira
- **PvP King 24h** - Competições de PvP contínuas
- **Party Zone** - Zonas de farm em grupo
- **Mission System** - Sistema de missões customizadas

### 🎨 **Sistemas Customizados**
- **Phantom System** - Sistema de bots inteligentes
- **Custom Balance** - Balanceamento personalizado por classe
- **Enchant System** - Sistema de encantamento avançado
- **VIP System** - Sistema de benefícios para VIPs
- **AIO System** - Sistema de buff automático

---

## 🛡️ Sistemas de Proteção

### **HWID Protection**
```ini
# Configurações de proteção por HWID
AllowGuardSystem = True
UseClientHWID = 2  # MAC Address
MaxPlayersPerHwid_inFlagZone = 1
MaxPlayersPerHwid_inBossZone = 1
MaxPlayersPerHwid_inSoloZone = 1
```

### **Anti-Bot System**
```ini
# Proteção contra bots
EnableBotsPrevention = True
KillsCounter = 300
ExtremeCaptcha = 20
ValidationTime = 60
```

### **Item Protection**
```ini
# Proteção de itens
NoDeleteItens = 6841,57,9500,9501,9502,9503,9504,9505,9506,9507,9508
NoSellItens = 6841
NoDropItens = 6841,57,9500,9501,9502,9503,9504,9505,9506,9507,9508
NoTradeItens = 6841,5556,5283,955,956,731,732,949,950
```

### **Multi-Box Protection**
```ini
# Controle de multi-boxing
AllowDualBox = True
AllowedBoxes = 9
MultiboxProtectionEnabled = True
ClientsPerPc = 10
```

---

## 🎯 Eventos Automáticos

### **Tournament System**
- **Modalidades**: 1x1, 2x2, 5x5, 9x9
- **Horários Automáticos**: Configuráveis
- **Recompensas**: Sistema de premiação
- **Restrições**: Controle de itens e skills
- **Arenas**: Múltiplas localizações

```ini
# Configuração do Tournament
TournamentStartTime = 01:00,03:00,09:00,11:00,13:00,17:00,19:00,23:00
TournamentEventTime = 45
Tour_MaxEnchant = 16
```

### **TvT (Team vs Team)**
- **Sistema de Clãs**: Competições entre clãs
- **Recompensas**: Itens e experiência
- **Horários**: Eventos automáticos
- **Balanceamento**: Sistema de classes

### **CTF (Capture The Flag)**
- **Modo Captura**: Sistema de bandeiras
- **Times**: Equipes balanceadas
- **Recompensas**: Sistema de pontos
- **Duração**: Eventos temporizados

### **PvP King 24h**
- **Competição Contínua**: 24 horas por dia
- **Ranking**: Sistema de pontuação
- **Recompensas**: Premiação diária
- **Proteção**: Anti-farm

---

## 🎨 Sistemas Customizados

### **Phantom System**
- **Bots Inteligentes**: NPCs que simulam jogadores
- **Farm Zones**: Zonas de farm automatizadas
- **PvP Events**: Participação em eventos
- **Customização**: Configuração de sets e skills

### **Custom Balance**
- **Classes Balanceadas**: Ajustes por classe
- **Skills Modificadas**: Chances customizadas
- **Physics**: Sistema de física personalizado
- **Olympiad**: Balanceamento específico para Oly

### **Enchant System**
- **Enchant Avançado**: Sistema customizado
- **Proteção**: Restrições de venda/drop
- **Limites**: Máximo de enchant configurável
- **Skills**: Skills de enchant especiais

### **VIP System**
- **Benefícios**: Vantagens exclusivas
- **Skills**: Skills especiais VIP
- **Recompensas**: Itens e buffs exclusivos
- **Acesso**: Zonas e eventos VIP

---

## ⚙️ Configurações Avançadas

### **Chat System**
```ini
# Sistema de chat avançado
DisableChat = False
DisableCapsLock = False
ChatAll_Protection = True
Talk_ChatAll_Time = 30
EnableChatLevel = True
ChatLevel = 76
```

### **Custom Spawn**
```ini
# Spawn customizado para novos jogadores
CustomSpawn = True
SpawnX_1 = 83410
SpawnY_1 = 148607
SpawnZ_1 = -3407
CustomStartingLvl = True
CharLvl = 20
```

### **URL System**
```ini
# Sistema de URLs automáticas
OpenUrlEnable = True
OpenUrlSite = http://www.facebook.com
News_url = https://www.l2whine.com/forum/
Donate_url = https://l2playfix.com/ucp/
```

---

## 🚀 Instalação

### **Requisitos**
- Java 8 ou superior
- MySQL 5.7+
- Windows/Linux

### **Passos de Instalação**

1. **Clone o repositório**
```bash
git clone https://github.com/ALN2025/pack_L2JALN.git
cd pack_L2JALN
```

2. **Configure o banco de dados**
```sql
-- Execute os scripts SQL fornecidos
-- Configure as credenciais no arquivo de configuração
```

3. **Configure os arquivos**
```bash
# Edite os arquivos de configuração
game/config/gameserver.ini
login/config/loginserver.ini
```

4. **Inicie os servidores**
```bash
# Login Server
cd login
./startLoginServer.sh

# Game Server
cd game
./startGameServer.sh
```

---

## ⚙️ Configuração

### **Arquivos Principais**
- `game/config/custom/L2_jaln.ini` - Configurações gerais
- `game/config/custom/Protect.ini` - Sistema de proteção
- `game/config/custom/events/` - Configurações de eventos
- `game/config/custom/phantom/` - Sistema Phantom
- `game/config/custom/balance/` - Balanceamento

### **Comandos Administrativos**
```bash
# Comandos disponíveis
//admin - Painel administrativo
//find_dualbox - Detectar multi-boxing
//phantom - Gerenciar sistema Phantom
//tournament - Gerenciar Tournament
```

---

## 📁 Estrutura do Projeto

```
pack_L2JALN/
├── game/                          # Game Server
│   ├── config/                    # Configurações
│   │   ├── custom/               # Configurações customizadas
│   │   │   ├── events/          # Eventos automáticos
│   │   │   ├── phantom/         # Sistema Phantom
│   │   │   ├── balance/         # Balanceamento
│   │   │   └── pvpzone/         # Zonas PvP
│   │   └── main/                # Configurações principais
│   ├── data/                     # Dados do servidor
│   │   ├── html/                # Interface HTML
│   │   ├── xml/                 # Arquivos XML
│   │   └── scripts/             # Scripts customizados
│   ├── libs/                     # Bibliotecas Java
│   └── log/                      # Logs do servidor
├── login/                         # Login Server
│   ├── config/                   # Configurações
│   ├── libs/                     # Bibliotecas
│   └── log/                      # Logs
└── bck.psc                       # Backup do banco
```

---

## 🎯 Funcionalidades Especiais

### **Sistema de Observação**
- NPC Observer para RaidBoss e GrandBoss
- Custo configurável por observação
- Logs detalhados de atividades

### **Sistema de Manutenção**
- Modo manutenção automático
- Mensagens customizadas
- Controle de acesso administrativo

### **Sistema de Anúncios**
- Anúncios automáticos de drops
- Sistema de eventos por chat
- Configuração de canais

### **Sistema de Farm Customizado**
- Zonas de farm personalizadas
- Drop customizado por mob
- Sistema de party farm

---

## 🛠️ Suporte

### **Documentação**
- [Wiki do Projeto](https://github.com/ALN2025/pack_L2JALN/wiki)
- [Guia de Configuração](docs/CONFIGURATION.md)
- [FAQ](docs/FAQ.md)

### **Comunidade**
- [Discord](https://discord.gg/l2jaln)
- [Forum](https://l2whine.com/forum/)
- [Telegram](https://t.me/l2jaln)

### **Desenvolvimento**
- **Autor**: ALN2025
- **Versão**: 1.0
- **Última Atualização**: Janeiro 2025

---

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## 🙏 Agradecimentos

- Comunidade L2J
- Contribuidores do projeto
- Testadores e feedback da comunidade

---

**⭐ Se este projeto te ajudou, considere dar uma estrela no GitHub!**

---

*Desenvolvido com ❤️ pela equipe L2Jaln* 