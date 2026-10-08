package kernel;

import area.map.GameCase;
import area.map.GameMap;
import client.Player;
import client.other.Stats;
import entity.monster.Monster;
import entity.mount.Mount;
import fight.spells.EffectConstant;
import fight.spells.SpellGrade;
import game.world.World;
import object.ObjectTemplate;
import org.apache.commons.lang3.ArrayUtils;
import util.RandomStats;

import java.util.*;

public class Constant {
    //DEBUG
    public static final int DEBUG_MAP_LIMIT = 30000;
    //DEBUG
    public static final int AUTO_CLEAN_MONTH = 9;
    //Fight
    public static final int TIME_START_FIGHT = 45000;
    public static final int TIME_BY_TURN = 30000;
    //Phoenix
    public static final String ALL_PHOENIX = "-11;-54|2;-12|-41;-17|5;-9|25;-4|36;5|12;12|10;19|-10;13|-14;31|-43;0|-60;-3|-58;18|24;-43|27;-33";


    public static final int ETAT_DIFFICILE = 101;
    public static final int ETAT_TRESDIFFICILE =102;
    public static final int ETAT_MONSTREUX = 103;
    public static final int ETAT_HOTOMANI = 104;
    public static final int SPELL_BOOSTBYDIFF = 999;

    public static final int MAX_SPAWN_IN_ARENA = 3;

    public static final int FIGHT_MAXIMAL_TURN = 666;
    public static final int FIGHT_MAXIMAL_TURN_DECO = 20;
    public static final int FIGHT_TYPE_CHALLENGE = 0;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //D�fies
    public static final int FIGHT_TYPE_AGRESSION = 1;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Aggros
    public static final int FIGHT_TYPE_CONQUETE = 2;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Conquete
    public static final int FIGHT_TYPE_DOPEUL = 3;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        //Dopeuls de temple
    public static final int FIGHT_TYPE_PVM = 4;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //PvM
    public static final int FIGHT_TYPE_PVT = 5;
    public static final int FIGHT_TYPE_GLADIATROOL = 8;
    //Percepteur
    public static final int FIGHT_STATE_INIT = 1;
    public static final int FIGHT_STATE_PLACE = 2;
    public static final int FIGHT_STATE_ACTIVE = 3;
    public static final int FIGHT_STATE_FINISHED = 4;
    //Items
    //Positions
    public static final int ITEM_POS_NO_EQUIPED = -1;
    public static final int ITEM_POS_AMULETTE = 0;
    public static final int ITEM_POS_ARME = 1;
    public static final int ITEM_POS_ANNEAU1 = 2;
    public static final int ITEM_POS_CEINTURE = 3;
    public static final int ITEM_POS_ANNEAU2 = 4;
    public static final int ITEM_POS_BOTTES = 5;
    public static final int ITEM_POS_COIFFE = 6;
    public static final int ITEM_POS_CAPE = 7;
    public static final int ITEM_POS_FAMILIER = 8;
    public static final int ITEM_POS_DOFUS1 = 9;
    public static final int ITEM_POS_DOFUS2 = 10;
    public static final int ITEM_POS_DOFUS3 = 11;
    public static final int ITEM_POS_DOFUS4 = 12;
    public static final int ITEM_POS_DOFUS5 = 13;
    public static final int ITEM_POS_DOFUS6 = 14;
    public static final int ITEM_POS_BOUCLIER = 15;
    public static final int ITEM_POS_DRAGODINDE = 16;
    //Objets dons, mutations, mal�diction, ..
    public static final int ITEM_POS_MUTATION = 20;
    public static final int ITEM_POS_ROLEPLAY_BUFF = 21;
    public static final int ITEM_POS_PNJ_SUIVEUR = 24;
    public static final int ITEM_POS_BENEDICTION = 23;
    public static final int ITEM_POS_MALEDICTION = 22;
    public static final int ITEM_POS_BONBON = 25;
    // Tonique pour gladiatrool
    public static final int ITEM_POS_TONIQUE_EQUILIBRAGE = 65;
    public static final int ITEM_POS_TONIQUE1 = 66;
    public static final int ITEM_POS_TONIQUE2 = 67;
    public static final int ITEM_POS_TONIQUE3 = 68;
    public static final int ITEM_POS_TONIQUE4 = 69;
    public static final int ITEM_POS_TONIQUE5 = 70;
    public static final int ITEM_POS_TONIQUE6 = 71;
    public static final int ITEM_POS_TONIQUE7 = 72;
    public static final int ITEM_POS_TONIQUE8 = 73;
    public static final int ITEM_POS_TONIQUE9 = 74;

    // ON PLACE LES WEBHOOK URL !
    public static String moderatorWebhook = "https://discord.com/api/webhooks/1142009964920066128/ysIHq1oBg_v4GKXcD63H_Ja-YTNY_nGUy82-LBD859KBrAx7ioS6xRnV8DZYnU0vfpmT";

    public static String GetNameByPos(int pos) {
        String equipement="";
        switch(pos){
            case ITEM_POS_NO_EQUIPED:
                equipement = "Pas équipé";
                break;
            case ITEM_POS_AMULETTE:
                equipement = "Amulette";
                break;
            case ITEM_POS_ARME:
                equipement = "Arme";
                break;
            case ITEM_POS_ANNEAU1:
                equipement = "Anneau 1";
                break;
            case ITEM_POS_CEINTURE:
                equipement = "Ceinture";
                break;
            case ITEM_POS_ANNEAU2:
                equipement = "Anneau 2";
                break;
            case ITEM_POS_BOTTES:
                equipement = "Botte";
                break;
            case ITEM_POS_COIFFE:
                equipement = "Coiffe";
                break;
            case ITEM_POS_CAPE:
                equipement = "Cape";
                break;
            case ITEM_POS_FAMILIER:
                equipement = "Familier";
                break;
            case ITEM_POS_DOFUS1:
                equipement = "Dofus 1";
                break;
            case ITEM_POS_DOFUS2:
                equipement = "Dofus 2";
                break;
            case ITEM_POS_DOFUS3:
                equipement = "Dofus 3";
                break;
            case ITEM_POS_DOFUS4:
                equipement = "Dofus 4";
                break;
            case ITEM_POS_DOFUS5:
                equipement = "Dofus 5";
                break;
            case ITEM_POS_DOFUS6:
                equipement = "Dofus 6";
                break;
            case ITEM_POS_BOUCLIER:
                equipement = "Bouclier";
                break;
            case ITEM_POS_DRAGODINDE:
                equipement = "Dragodinde";
                break;
        }
        return equipement;
    }
    //Types
    public static final List<Integer> ITEM_TYPE_TO_SELL = Arrays.asList(1,9,12,14,26,43,44,45,66,70,71,86,18,72,77,90,97,113,116,63,64,69,33,42,84,93,112,114,38,95,96,98,108,10,11,13,25,73,75,76,5,6,7,8,19,20,21,22,39,40,50,51,88,87,34,52,60,41,49,62,15,35,36,46,47,48,53,54,55,56,57,58,59,65,68,103,105,106,107,109,110,111,78,2,3,4,16,17,81,83,85);
    public static final int[] ITEM_TYPE_OBJ_BLACK = {1,2,3,4,5,6,7,8,9,10,11,16,17,19,21,22,81};
    public static final Integer[] ITEM_TYPE_OBJ_BLACK2 = new Integer[]{1,2,3,4,5,6,7,8,9,10,11,16,17,19,21,22,81};// 1,2,3,4,5,6,7,8,9,10,11,16,17,19,21,22,81
    public static final int[] ITEM_TYPE_WITH_RARITY = {1,2,3,4,5,6,7,8,9,10,11,16,17,19,21,22,23,81};
    public static final int[] SUPERTYPE_NOT_EQUIPABLE = {9,14,15,16,17,18,6,19,21,20,8,22};



    public static final int[]  ITEM_SUPERTYPE_AMU ={1};
    public static final int[]  ITEM_SUPERTYPE_ARME ={2,3,4,5,6,7,8,19,20,21,22,102,114};
    public static final int[]  ITEM_SUPERTYPE_ANNEAU ={9};
    public static final int[]  ITEM_SUPERTYPE_CEINTURE ={10};
    public static final int[]  ITEM_SUPERTYPE_BOTTE ={11};
    public static final int[]  ITEM_SUPERTYPE_LAUNCHABLE ={12,13,14,25,33,42,43,44,45,49,69,70,72,73,74,75,76,77,79,80,85,86,87,88,89,93,94,97,100,112,115,116};
    public static final int[]  ITEM_SUPERTYPE_BOUCLIER = {82};
    public static final int[]  ITEM_SUPERTYPE_CAPTURE = {83,99};
    public static final int[]  ITEM_SUPERTYPE_RESSOURCES ={15,26,34,35,36,37,38,39,40,41,46,47,48,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,68,71,78,84,90,95,96,98,103,104,105,106,107,108,109,110,111};
    public static final int[]  ITEM_SUPERTYPE_COIFFE ={16};
    public static final int[]  ITEM_SUPERTYPE_CAPE ={17,81};
    public static final int[]  ITEM_SUPERTYPE_FAMI ={18};
    public static final int[]  ITEM_SUPERTYPE_DOFUS ={23};
    public static final int[]  ITEM_SUPERTYPE_QUEST ={24};
    public static final int[]  ITEM_SUPERTYPE_MUTATION ={27};
    public static final int[]  ITEM_SUPERTYPE_BOOSTFOOD ={28};
    public static final int[]  ITEM_SUPERTYPE_BENE ={29};
    public static final int[]  ITEM_SUPERTYPE_MALE ={30};
    public static final int[]  ITEM_SUPERTYPE_RPBUFF ={31};
    public static final int[]  ITEM_SUPERTYPE_SUIVEUR ={32};
    public static final int[]  ITEM_SUPERTYPE_MONTURE = {91,92};
    public static final int[]  ITEM_SUPERTYPE_OBVJ = {113};

    public static final int[] FILTER_EQUIPEMENT_1 = ArrayUtils.addAll(ITEM_SUPERTYPE_AMU,ITEM_SUPERTYPE_ARME);
    public static final int[] FILTER_EQUIPEMENT_2 = ArrayUtils.addAll(ITEM_SUPERTYPE_ANNEAU,ITEM_SUPERTYPE_CEINTURE);
    public static final int[] FILTER_EQUIPEMENT_3 = ArrayUtils.addAll(ITEM_SUPERTYPE_BOTTE,ITEM_SUPERTYPE_BOUCLIER);
    public static final int[] FILTER_EQUIPEMENT_4 = ArrayUtils.addAll(ITEM_SUPERTYPE_CAPTURE,ITEM_SUPERTYPE_COIFFE); //ArrayUtils.addAll(ITEM_SUPERTYPE_CAPTURE,ITEM_SUPERTYPE_COIFFE);
    public static final int[] FILTER_EQUIPEMENT_5 = ArrayUtils.addAll(ITEM_SUPERTYPE_CAPE,ITEM_SUPERTYPE_FAMI);
    public static final int[] FILTER_EQUIPEMENT_6 = ArrayUtils.addAll(ITEM_SUPERTYPE_AMU,ITEM_SUPERTYPE_ARME);
    public static final int[] FILTER_EQUIPEMENT_1_1 = ArrayUtils.addAll(FILTER_EQUIPEMENT_1,FILTER_EQUIPEMENT_2);
    public static final int[] FILTER_EQUIPEMENT_1_2 = ArrayUtils.addAll(FILTER_EQUIPEMENT_3,FILTER_EQUIPEMENT_4);
    public static final int[] FILTER_EQUIPEMENT_1_3 = ArrayUtils.addAll(FILTER_EQUIPEMENT_5,FILTER_EQUIPEMENT_6);
    public static final int[] FILTER_EQUIPEMENT_2_4 = ArrayUtils.addAll(FILTER_EQUIPEMENT_1_1,FILTER_EQUIPEMENT_1_2);
    public static final int[] FILTER_EQUIPEMENT_2_5 = ArrayUtils.addAll(FILTER_EQUIPEMENT_1_3,ITEM_SUPERTYPE_DOFUS);


    public static final int[] FILTER_EQUIPEMENT = ArrayUtils.addAll(FILTER_EQUIPEMENT_2_4,FILTER_EQUIPEMENT_2_5);
    public static final int[] FILTER_NONEQUIPEMENT = ITEM_SUPERTYPE_LAUNCHABLE;
    public static final int[] FILTER_RESSOURCES = ITEM_SUPERTYPE_RESSOURCES;
    public static final int[] FILTER_QUEST = ITEM_SUPERTYPE_QUEST;

    public static final int ITEM_TYPE_AMULETTE = 1;
    public static final int ITEM_TYPE_ARC = 2;
    public static final int ITEM_TYPE_BAGUETTE = 3;
    public static final int ITEM_TYPE_BATON = 4;
    public static final int ITEM_TYPE_DAGUES = 5;
    public static final int ITEM_TYPE_EPEE = 6;
    public static final int ITEM_TYPE_MARTEAU = 7;
    public static final int ITEM_TYPE_PELLE = 8;
    public static final int ITEM_TYPE_ANNEAU = 9;
    public static final int ITEM_TYPE_CEINTURE = 10;
    public static final int ITEM_TYPE_BOTTES = 11;
    public static final int ITEM_TYPE_POTION = 12;
    public static final int ITEM_TYPE_PARCHO_EXP = 13;
    public static final int ITEM_TYPE_DONS = 14;
    public static final int ITEM_TYPE_RESSOURCE = 15;
    public static final int ITEM_TYPE_COIFFE = 16;
    public static final int ITEM_TYPE_CAPE = 17;
    public static final int ITEM_TYPE_FAMILIER = 18;
    public static final int ITEM_TYPE_HACHE = 19;
    public static final int ITEM_TYPE_OUTIL = 20;
    public static final int ITEM_TYPE_PIOCHE = 21;
    public static final int ITEM_TYPE_FAUX = 22;
    public static final int ITEM_TYPE_DOFUS = 23;
    public static final int ITEM_TYPE_QUETES = 24;
    public static final int ITEM_TYPE_DOCUMENT = 25;
    public static final int ITEM_TYPE_FM_POTION = 26;
    public static final int ITEM_TYPE_TRANSFORM = 27;
    public static final int ITEM_TYPE_BOOST_FOOD = 28;
    public static final int ITEM_TYPE_BENEDICTION = 29;
    public static final int ITEM_TYPE_MALEDICTION = 30;
    public static final int ITEM_TYPE_RP_BUFF = 31;
    public static final int ITEM_TYPE_PERSO_SUIVEUR = 32;
    public static final int ITEM_TYPE_PAIN = 33;
    public static final int ITEM_TYPE_CEREALE = 34;
    public static final int ITEM_TYPE_FLEUR = 35;
    public static final int ITEM_TYPE_PLANTE = 36;
    public static final int ITEM_TYPE_BIERE = 37;
    public static final int ITEM_TYPE_BOIS = 38;
    public static final int ITEM_TYPE_MINERAIS = 39;
    public static final int ITEM_TYPE_ALLIAGE = 40;
    public static final int ITEM_TYPE_POISSON = 41;
    public static final int ITEM_TYPE_BONBON = 42;
    public static final int ITEM_TYPE_POTION_OUBLIE = 43;
    public static final int ITEM_TYPE_POTION_METIER = 44;
    public static final int ITEM_TYPE_POTION_SORT = 45;
    public static final int ITEM_TYPE_FRUIT = 46;
    public static final int ITEM_TYPE_OS = 47;
    public static final int ITEM_TYPE_POUDRE = 48;
    public static final int ITEM_TYPE_COMESTI_POISSON = 49;
    public static final int ITEM_TYPE_PIERRE_PRECIEUSE = 50;
    public static final int ITEM_TYPE_PIERRE_BRUTE = 51;
    public static final int ITEM_TYPE_FARINE = 52;
    public static final int ITEM_TYPE_PLUME = 53;
    public static final int ITEM_TYPE_POIL = 54;
    public static final int ITEM_TYPE_ETOFFE = 55;
    public static final int ITEM_TYPE_CUIR = 56;
    public static final int ITEM_TYPE_LAINE = 57;
    public static final int ITEM_TYPE_GRAINE = 58;
    public static final int ITEM_TYPE_PEAU = 59;
    public static final int ITEM_TYPE_HUILE = 60;
    public static final int ITEM_TYPE_PELUCHE = 61;
    public static final int ITEM_TYPE_POISSON_VIDE = 62;
    public static final int ITEM_TYPE_VIANDE = 63;
    public static final int ITEM_TYPE_VIANDE_CONSERVEE = 64;
    public static final int ITEM_TYPE_QUEUE = 65;
    public static final int ITEM_TYPE_METARIA = 66;
    public static final int ITEM_TYPE_LEGUME = 68;
    public static final int ITEM_TYPE_VIANDE_COMESTIBLE = 69;
    public static final int ITEM_TYPE_TEINTURE = 70;
    public static final int ITEM_TYPE_EQUIP_ALCHIMIE = 71;
    public static final int ITEM_TYPE_OEUF_FAMILIER = 72;
    public static final int ITEM_TYPE_MAITRISE = 73;
    public static final int ITEM_TYPE_FEE_ARTIFICE = 74;
    public static final int ITEM_TYPE_PARCHEMIN_SORT = 75;
    public static final int ITEM_TYPE_PARCHEMIN_CARAC = 76;
    public static final int ITEM_TYPE_CERTIFICAT_CHANIL = 77;
    public static final int ITEM_TYPE_RUNE_FORGEMAGIE = 78;
    public static final int ITEM_TYPE_BOISSON = 79;
    public static final int ITEM_TYPE_OBJET_MISSION = 80;
    public static final int ITEM_TYPE_SAC_DOS = 81;
    public static final int ITEM_TYPE_BOUCLIER = 82;
    public static final int ITEM_TYPE_PIERRE_AME = 83;
    public static final int ITEM_TYPE_CLEFS = 84;
    public static final int ITEM_TYPE_PIERRE_AME_PLEINE = 85;
    public static final int ITEM_TYPE_POPO_OUBLI_PERCEP = 86;
    public static final int ITEM_TYPE_PARCHO_RECHERCHE = 87;
    public static final int ITEM_TYPE_PIERRE_MAGIQUE = 88;
    public static final int ITEM_TYPE_CADEAUX = 89;
    public static final int ITEM_TYPE_FANTOME_FAMILIER = 90;
    public static final int ITEM_TYPE_DRAGODINDE = 91;
    public static final int ITEM_TYPE_BOUFTOU = 92;
    public static final int ITEM_TYPE_OBJET_ELEVAGE = 93;
    public static final int ITEM_TYPE_OBJET_UTILISABLE = 94;
    public static final int ITEM_TYPE_PLANCHE = 95;
    public static final int ITEM_TYPE_ECORCE = 96;
    public static final int ITEM_TYPE_CERTIF_MONTURE = 97;
    public static final int ITEM_TYPE_RACINE = 98;
    public static final int ITEM_TYPE_FILET_CAPTURE = 99;
    public static final int ITEM_TYPE_SAC_RESSOURCE = 100;
    public static final int ITEM_TYPE_ARBALETE = 102;
    public static final int ITEM_TYPE_PATTE = 103;
    public static final int ITEM_TYPE_AILE = 104;
    public static final int ITEM_TYPE_OEUF = 105;
    public static final int ITEM_TYPE_OREILLE = 106;
    public static final int ITEM_TYPE_CARAPACE = 107;
    public static final int ITEM_TYPE_BOURGEON = 108;
    public static final int ITEM_TYPE_OEIL = 109;
    public static final int ITEM_TYPE_GELEE = 110;
    public static final int ITEM_TYPE_COQUILLE = 111;
    public static final int ITEM_TYPE_PRISME = 112;
    public static final int ITEM_TYPE_OBJET_VIVANT = 113;
    public static final int ITEM_TYPE_ARME_MAGIQUE = 114;
    public static final int ITEM_TYPE_FRAGM_AME_SHUSHU = 115;
    public static final int ITEM_TYPE_POTION_FAMILIER = 116;
    public static final int ITEM_TYPE_CARTE_COMMUNE = 119;
    public static final int ITEM_TYPE_CARTE_RARE = 120;
    public static final int ITEM_TYPE_CARTE_EPIQUE = 121;
    public static final int ITEM_TYPE_CARTE_ULTIME = 122;
    public static final int ITEM_TYPE_PACKET_CARTE = 123;
    public static final int ITEM_TYPE_PIERRE_AME_PLEINE_BOSS = 124;
    public static final int ITEM_TYPE_PIERRE_AME_PLEINE_ARCHI = 125;
    public static final int ITEM_TYPE_TONIQUE = 126;
    public static final int ITEM_TYPE_SPECIAL = 127;

    //Monstre
    public static final int[] MONSTRE_TYPE_DIVERS = {-1,29,32,18,28,27,0,1,68,50,79};
    public static final int[] MONSTRE_TYPE_CHAMPS = {11,47,9,10,45,46};
    public static final int[] MONSTRE_TYPE_MONTAGNE = {16,12,52,6,13,5};
    public static final int[] MONSTRE_TYPE_FORET = {38,37,49,39,4,22,7};
    public static final int[] MONSTRE_TYPE_PLAINE = {24,23,51,21,2};
    public static final int[] MONSTRE_TYPE_LANDES = {25};
    public static final int[] MONSTRE_TYPE_ILE_MOON = {43,42,41,20,40};
    public static final int[] MONSTRE_TYPE_ILE_WABBIT = {3};
    public static final int[] MONSTRE_TYPE_PANDALA = {36,35,34,57,56,55,59};
    public static final int[] MONSTRE_TYPE_HUMAIN = {26,30,19};
    public static final int[] MONSTRE_TYPE_NUIT = {8,53,54,17};
    public static final int[] MONSTRE_TYPE_MARECAGE = {48,44};
    public static final int[] MONSTRE_TYPE_VILLE = {33,31};
    public static final int[] MONSTRE_TYPE_VILLAGE_ELEVEUR = {60,61};
    public static final int[] MONSTRE_TYPE_RESSOURCE_PROTECTEUR = {62,63,64,65,66};
    public static final int[] MONSTRE_TYPE_ILE_MINO = {67};
    public static final int[] MONSTRE_TYPE_PLAGES = {69};
    public static final int[] MONSTRE_TYPE_INCARNAM ={70};
    public static final int[] MONSTRE_TYPE_ILE_OTO = {71,72,73,74,75,76,77};
    public static final int[] MONSTRE_TYPE_ARCHI = {78};

    public static final int[] FILTER_MONSTRE_SPE1 = ArrayUtils.addAll(MONSTRE_TYPE_DIVERS,MONSTRE_TYPE_ARCHI);
    public static final int[] FILTER_MONSTRE_SPE2 = ArrayUtils.addAll(FILTER_MONSTRE_SPE1,MONSTRE_TYPE_HUMAIN);
    public static final int[] FILTER_MONSTRE_SPE = ArrayUtils.addAll(FILTER_MONSTRE_SPE2,MONSTRE_TYPE_RESSOURCE_PROTECTEUR);
    public static final int[] EXCLUDE_MOBID_TODROP = {404,1088};

    public static final int MONSTRES_NON_CLASSE = -1;
    public static final int MONSTRES_INVOCATIONS_DE_CLASSE = 0;
    public static final int MONSTRES_BOSS = 1;
    public static final int MONSTRES_BANDITS = 2;
    public static final int MONSTRES_WABBITS = 3;
    public static final int MONSTRES_DRAGOEUFS = 4;
    public static final int MONSTRES_BWORKS = 5;
    public static final int MONSTRES_GOBELINS = 6;
    public static final int MONSTRES_GELEES = 7;
    public static final int MONSTRES_DE_LA_NUIT = 8;
    public static final int MONSTRES_BOUFTOUS = 9;
    public static final int MONSTRES_PLANTES_DES_CHAMPS = 10;
    public static final int MONSTRES_LARVES = 11;
    public static final int MONSTRES_KWAKS = 12;
    public static final int MONSTRES_CRAQUELEURS = 13;
    public static final int MONSTRES_COCHONS = 16;
    public static final int MONSTRES_CHAFERS = 17;
    public static final int MONSTRES_DOPEULS_TEMPLE = 18;
    public static final int MONSTRES_PNJS = 19;
    public static final int MONSTRES_KANNIBOULS_DE_ILE_DE_MOON = 20;
    public static final int MONSTRES_DRAGODINDE = 21;
    public static final int MONSTRES_ABRAKNYDIEN = 22;
    public static final int MONSTRES_BLOP = 23;
    public static final int MONSTRES_DES_PLAINES_DE_CANIA = 24;
    public static final int MONSTRES_DES_LANDES = 25;
    public static final int MONSTRES_GARDES = 26;
    public static final int MONSTRES_DES_CONQUETES_DE_TERRITOIRES = 27;
    public static final int MONSTRES_DU_VILLAGE_DES_DOPEULS = 28;
    public static final int MONSTRES_TUTORIAL = 29;
    public static final int MONSTRES_BRIGANDINS = 30;
    public static final int MONSTRE_DES_EGOUTS = 31;
    public static final int MONSTRES_AVIS_DE_RECHERCHE = 32;
    public static final int MONSTRES_PIOUS = 33;
    public static final int MONSTRES_DU_VILLAGE_DE_PANDALA = 34;
    public static final int MONSTRES_DE_PANDALA = 35;
    public static final int MONSTRES_FANTOME_DE_PANDALA = 36;
    public static final int MONSTRES_SCARAFEUILLE = 37;
    public static final int MONSTRES_ARAKNE = 38;
    public static final int MONSTRES_MULOU = 39;
    public static final int MONSTRES_TORTUES_DE_MOON = 40;
    public static final int MONSTRES_PIRATES_DE_MOON = 41;
    public static final int MONSTRES_PLANTES_DE_MOON = 42;
    public static final int MONSTRES_DE_MOON = 43;
    public static final int MONSTRES_CROCODAILLES = 44;
    public static final int MONSTRES_CHAMPIGNONS = 45;
    public static final int MONSTRES_TOFUS = 46;
    public static final int MONSTRES_MOSKITOS = 47;
    public static final int MONSTRES_DES_MARECAGES = 48;
    public static final int MONSTRES_ANIMAUX_DE_LA_FORET = 49;
    public static final int MONSTRES_DE_QUETE = 50;
    public static final int MONSTRES_CORBACS = 51;
    public static final int MONSTRES_GARDIENS_DES_VILLAGES_DE_KWAKS = 52;
    public static final int MONSTRES_FANTOMES = 53;
    public static final int MONSTRES_FAMILIERS_FANTOMES = 54;
    public static final int MONSTRES_PLANTES_DE_PANDALA = 55;
    public static final int MONSTRES_KITSOUS = 56;
    public static final int MONSTRES_PANDAWAS = 57;
    public static final int MONSTRES_FIREFOUX = 59;
    public static final int MONSTRES_KOALAKS = 60;
    public static final int MONSTRES_DES_CAVERNES = 61;
    public static final int MONSTRES_PROTECTEURS_DES_CEREALES = 62;
    public static final int MONSTRES_PROTECTEURS_DES_MINERAIS = 63;
    public static final int MONSTRES_PROTECTEURS_DES_ARBRES = 64;
    public static final int MONSTRES_PROTECTEURS_DES_POISSONS = 65;
    public static final int MONSTRES_PROTECTEURS_DES_PLANTES = 66;
    public static final int MONSTRES_MINOS = 67;
    public static final int MONSTRES_DE_NOWEL = 68;
    public static final int MONSTRES_DES_PLAGES = 69;
    public static final int MONSTRES_DE_LA_ZONE_DES_DEBUTANTS = 70;
    public static final int MONSTRES_DES_PLAINES_HERBEUSES = 71;
    public static final int MONSTRES_DE_LA_PLAGE_DE_CORAIL = 72;
    public static final int MONSTRES_DE_LA_TOURBIERE_SANS_FOND = 73;
    public static final int MONSTRES_DE_LA_JUNGLE_SOMBRE = 74;
    public static final int MONSTRES_DE_ARBRE_HAKAM = 75;
    public static final int MONSTRES_DE_ARCHE_DOTOMAI = 76;
    public static final int MONSTRES_DE_LA_CANOPEE_EMBRUMEE = 77;
    public static final int MONSTRES_LES_ARCHIMONSTRES = 78;
    public static final int MONSTRES_TO_VERIF = 79;

    public static int[] BOSS_ID = {58,85,86,107,113,121,147,173,180,226,230,232,251,257,289,295,382,404,423,430,457,478,568,605,612,669,670,780,792,797,799,800,827,854,865,866,926,939,940,943,1015,1027,1045,1051,1071,1072,1085,1086,1087,1159,1170,1184,1185,1186,1187,1188,1195};
    public static int[] BOSS_HOTOMANI_ID = {58,85,86,107,113,121,147,173,180,225,226,230,232,251,252,257,289,295,374,375,377,382,404,423,430,457,478,568,605,612,669,670,673,675,677,681,780,792,797,799,800,827,854,926,939,940,943,1015,1027,1045,1051,1071,1072,1085,1086,1087,1159,1170,1184,1185,1186,1187,1188,1195};

    public static int[] EXCEPTION_HOTOMANI_BOSS = {251,295,404,423,450,1159,865,866,1195,1170};
    public static int[] EXCEPTION_HOTOMANI_ARCHI = {251,404,423,450,1159};
    public static int[] EXCEPTION_HOTOMANI_MONSTRES = {258,260,251,404,424,450,1090,1091,1092,1094,1088};

    public static int[] EXCEPTION_GLADIATROOL_BOSS = {295,404,423,1159};
    public static int[] EXCEPTION_GLADIATROOL_ARCHI = {404,423,1159};
    public static int[] EXCEPTION_GLADIATROOL_MONSTRES = {258,260,404,424,1090,1091,1092,1094,1088};

    public static final List<Integer> GLADIATROOL_FULLMORPHID = Arrays.asList(101,102,103,104,105,106,107,108,109,110,111,112);

    public static int[] HOTOMANI_MAPID = {12012,12023,12001,12006,12007,12008,12004,12009};
    public static int[] HOTOMANIDJ_MAPID = {12010,12017,12000,12015,12014,12028};

    //public static int[] GLADIATROOL_MAPID = {12012,12023,12001,12006,12007,12008,12004,12009};
    public static int[] ARENA_MAPID = {10131,10132,10133,10134,10135,10136,10137,10138};

    // Bouclier exception arme a deux main
    public static int[] SHIELD_HANDLING_EXCEPTIONS = {11621,11714};

   // Dégat de poussé
   public static final String DO_POU_DOMMAGE = "1d8+8";

    //Alignement
    public static final int ALIGNEMENT_NEUTRE = -1;
    public static final int ALIGNEMENT_BONTARIEN = 1;
    public static final int ALIGNEMENT_BRAKMARIEN = 2;
    public static final int ALIGNEMENT_MERCENAIRE = 3;

    //Elements
    public static final int ELEMENT_NULL = -1;
    public static final int ELEMENT_NEUTRE = 0;
    public static final int ELEMENT_TERRE = 1;
    public static final int ELEMENT_EAU = 2;
    public static final int ELEMENT_FEU = 3;
    public static final int ELEMENT_AIR = 4;
    //Classes
    public static final int CLASS_FECA = 1;
    public static final int CLASS_OSAMODAS = 2;
    public static final int CLASS_ENUTROF = 3;
    public static final int CLASS_SRAM = 4;
    public static final int CLASS_XELOR = 5;
    public static final int CLASS_ECAFLIP = 6;
    public static final int CLASS_ENIRIPSA = 7;
    public static final int CLASS_IOP = 8;
    public static final int CLASS_CRA = 9;
    public static final int CLASS_SADIDA = 10;
    public static final int CLASS_SACRIEUR = 11;
    public static final int CLASS_PANDAWA = 12;
    public static final int CLASS_MULTIMAN = 13;
    //Sexes
    public static final int SEX_MALE = 0;
    public static final int SEX_FEMALE = 1;
    //GamePlay
    public static final int MAX_EFFECTS_ID = 2500;

    //Buff a v�rifier en d�but de tour
    public static final int[] BEGIN_TURN_BUFF = {91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 108};

    //Buff des Armes
    public static final int[] ARMES_EFFECT_IDS = {91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 108};
    //Buff a ne pas booster en cas de CC
    public static final int[] NO_BOOST_CC_IDS = {101};

    //Panoplie de Classe
    public static final int[] BUFF_SET_CLASSE = {281, 282, 283, 284, 285, 286, 287, 288, 289, 290, 291, 292, 293};
    //Invocation Statiques
    public static final int[] STATIC_INVOCATIONS = {282, 556, 2750, 7000};                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    //Arbre et Cawotte s'tout :p

    //Buff d�clench� en cas de frappe
    public static final int[] ON_HIT_BUFFS = {9, 79, 107, 788, 606, 607, 608, 609, 611};

    //ITEM ID SPECIFIC REMOVE DROP
    public static final int[] ITEMS_EXCLUDE_DROP = {12782, 12783, 12784, 12785, 12786, 12787, 12788, 12789, 12790, 12791, 12792, 12793, 11745, 11746, 11761};

    public static final int[] SPELLEFFECT_DAMMAGE = {5,6,8,82,85,86,87,88,89,91,92,93,94,95,96,97,98,99,100,130,131,141,671,672,776,275,276,277,278,279,2127};
    public static final int[] SPELLEFFECT_DEBUFF = {77,84,101,116,122,127,132,140,144,145,152,153,154,155,156,157,162,163,168,169,171,177,179,186,215,216,217,218,219,245,246,247,248,249,266,267,268,269,270,271,320,781,172};
    public static final int[] SPELLEFFECT_BUFF = {9,78,79,105,106,107,110,111,112,114,115,117,118,119,120,121,123,124,125,126,128,138,142,150,160,161,164,165,176,178,182,183,184,202,210,211,212,213,214,220,240,241,242,243,244,265,284,285,287,290,293,765,782,787,788,950,951,135,136,606,607,608,609,610,611};
    public static final int[] SPELLEFFECT_HEAL = {81,90,108,143,786};
    public static final int[] SPELLEFFECT_INVO = {180,181,185,200,405,780,201};
    public static final int[] SPELLEFFECT_MOUVEMENT = {4,50,51,783,784};
    public static final int[] SPELLEFFECT_USELESS = {149,666,333,750,751,109};
    public static final int[] SPELLEFFECT_TRAP = {400,401,402,1000,1001,1002};


    public static final int[] STATIC_INVO = {556,2818,2817,2816,2815,282};


    public static final ArrayList<Integer> ISSPIRITGEM = getGemmesSpritiuelsID();
    public static final int[] SPIRITGEMID = ISSPIRITGEM.stream().mapToInt(i -> i).toArray();




    public static final String COULEUR_SUCCES = "009404";
    public static final String COULEUR_ECHEC = "A82214";
    public static final String COULEUR_INFO = "095DAB";



    //Effets ID & Buffs
    public static final int EFFECT_PASS_TURN = 140;

    public static final int STATS_FORGET_ONE_LEVEL_SPELL = 616;
    //Capture
    public static final int CAPTURE_MONSTRE1 = 621;
    public static final int CAPTURE_MONSTRE = 623;
    public static final int CAPTURE_MONSTRE2 = 628;
    //Familier
    public static final int STATS_PETS_PDV = 800;
    public static final int STATS_PETS_POIDS = 806;
    public static final int STATS_PETS_REPAS = 807;
    public static final int STATS_PETS_DATE = 808;
    public static final int STATS_PETS_EPO = 940;
    public static final int STATS_PETS_SOUL = 717;
    // Objet d'�levage
    public static final int STATS_RESIST = 812;
    public static final int STATS_DD_ID = 995;
    public static final int STATS_DD_OWNER = 996;
    public static final int STATS_DD_NAME = 997;

    // Other
    public static final int STATS_TURN = 811;
    public static final int STATS_EXCHANGE_IN = 983;
    public static final int STATS_CHANGE_BY = 985;
    public static final int STATS_OWNER_1 = 987;//#4
    public static final int STATS_BUILD_BY = 988;
    public static final int STATS_NAME_TRAQUE = 989;
    public static final int STATS_GRADE_TRAQUE = 961;
    public static final int STATS_ALIGNEMENT_TRAQUE = 960;
    public static final int STATS_NIVEAU_TRAQUE = 962;

    public static final int STATS_DATE = 805;

    public static final int STATS_NIVEAU = 962;
    public static final int OBVIJEVANT_APPARENCE = 970;
    public static final int OBVIJEVANT_HUMOR = 971;
    public static final int OBVIJEVANT_SKIN = 972;
    public static final int OBVIJEVANT_TYPE = 973;
    //public static final int OBVIJEVANT_TYPE = 974;


    public static final int STATS_NAME_DJ = 814;
    //public static final int MIMIBIOTE = 915;
    public static final int STATS_SIGNATURE = 988;
    public static final int ERR_STATS_XP = 1000;
    public static final int COMPATIBLE_AVEC = 1003;
    public static final int APPARAT_ITEM = 915;
    public static final int APPARAT_NAME2 = 916;
    public static final int APPARAT_NAME = 969;






    //ZAAPI <alignID,{mapID,mapID,...,mapID}>
    public static Map<Integer, String> ZAAPI = new HashMap<Integer, String>();
    //ZAAP <mapID,cellID>
    public static Map<Integer, Integer> ZAAPS = new HashMap<Integer, Integer>();
    //Valeur des droits de guilde
    public static int[] G_RIGHTS = new int[] {2, 4, 8, 16, 32, 64, 128, 256, 512, 4096, 8192, 16384};
    public static int G_BOOST = 2;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //G�rer les boost
    public static int G_RIGHT = 4;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //G�rer les droits
    public static int G_INVITE = 8;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Inviter de nouveaux membres
    public static int G_BAN = 16;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Bannir
    public static int G_ALLXP = 32;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //G�rer les r�partitions d'xp
    public static int G_HISXP = 256;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //G�rer sa r�partition d'xp
    public static int G_RANK = 64;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //G�rer les rangs
    public static int G_POSPERCO = 128;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Poser un percepteur
    public static int G_COLLPERCO = 512;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Collecter les percepteurs
    public static int G_USEENCLOS = 4096;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Utiliser les enclos
    public static int G_AMENCLOS = 8192;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Am�nager les enclos
    public static int G_OTHDINDE = 16384;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        //G�rer les montures des autres membres
    //Valeur des droits de maison
    public static int H_GBLASON = 2;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Afficher blason pour membre de la guilde
    public static int H_OBLASON = 4;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Afficher blason pour les autres
    public static int H_GNOCODE = 8;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Entrer sans code pour la guilde
    public static int H_OCANTOPEN = 16;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Entrer impossible pour les non-guildeux
    public static int C_GNOCODE = 32;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Coffre sans code pour la guilde
    public static int C_OCANTOPEN = 64;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Coffre impossible pour les non-guildeux
    public static int H_GREPOS = 256;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Guilde droit au repos
    public static int H_GTELE = 128;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            //Guilde droit a la TP
    // Nom des documents (swfs) : Documents d'avis de recherche
    public static String HUNT_DETAILS_DOC = "71_0706251229";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // PanMap d'explications
    public static String HUNT_FRAKACIA_DOC = "63_0706251124";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Frakacia Leukocythine
    public static String HUNT_AERMYNE_DOC = "100_0706251214";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            // Aermyne 'Braco' Scalptaras
    public static String HUNT_MARZWEL_DOC = "96_0706251201";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Marzwel le Gobelin
    public static String HUNT_BRUMEN_DOC = "68_0706251126";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Brumen Tinctorias
    public static String HUNT_MUSHA_DOC = "94_0706251138";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Musha l'Oni
    public static String HUNT_OGIVOL_DOC = "69_0706251058";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Ogivol Scarlacin
    public static String HUNT_PADGREF_DOC = "61_0802081743";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Padgref Demoel
    public static String HUNT_QILBIL_DOC = "67_0706251223";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Qil Bil
    public static String HUNT_ROK_DOC = "93_0706251135";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Rok Gnorok
    public static String HUNT_ZATOISHWAN_DOC = "98_0706251211";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Zatoïshwan
    public static String HUNT_LETHALINE_DOC = "65_0706251123";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                // Léthaline Sigisbul
    //public static String HUNT_NERVOES_DOC    = "64_0706251123";  // Nervoes Brakdoun
    public static String HUNT_FOUDUGLEN_DOC = "70_0706251122"; // Fouduglen l'�cureuil


    // {(int)BorneId, (int)CellId, (str)SwfDocName, (int)MobId, (int)ItemFollow, (int)QuestId, (int)reponseID
    public static String[][] HUNTING_QUESTS = {{"1988", "234", HUNT_DETAILS_DOC, "-1", "-1", "-1", "-1"}, {"1986", "161", HUNT_LETHALINE_DOC, "-1", "-1", "-1", "-1"}, {"1985", "119", HUNT_MARZWEL_DOC, "554", "7353", "117", "2552"}, {"1986", "120", HUNT_PADGREF_DOC, "459", "6870", "29", "2108"}, {"1985", "149", HUNT_FRAKACIA_DOC, "460", "6871", "30", "2109"}, {"1986", "150", HUNT_QILBIL_DOC, "481", "6873", "32", "2111"}, {"1986", "179", HUNT_BRUMEN_DOC, "464", "6874", "33", "2112"}, {"1986", "180", HUNT_OGIVOL_DOC, "462", "6876", "35", "2114"}, {"1985", "269", HUNT_MUSHA_DOC, "552", "7352", "116", "2551"}, {"1986", "270", HUNT_FOUDUGLEN_DOC, "463", "6875", "34", "2113"}, {"1985", "299", HUNT_ROK_DOC, "550", "7351", "115", "2550"}, {"1986", "300", HUNT_AERMYNE_DOC, "446", "7350", "119", "2554"}, {"1985", "329", HUNT_ZATOISHWAN_DOC, "555", "7354", "118", "2553"},};

    // SpellPlaces
    public static List<Character> SPELL_PLACES =  Arrays.asList('a','b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q');

    public static int getQuestByMobSkin(int mobSkin) {
        for (int v = 0; v < HUNTING_QUESTS.length; v++)
            if (World.world.getMonstre(Integer.parseInt(HUNTING_QUESTS[v][3])) != null
                    && World.world.getMonstre(Integer.parseInt(HUNTING_QUESTS[v][3])).getGfxId() == mobSkin)
                return Integer.parseInt(HUNTING_QUESTS[v][5]);
        return -1;
    }

    public static int getSkinByHuntMob(int mobId) {
        for (int v = 0; v < HUNTING_QUESTS.length; v++)
            if (Integer.parseInt(HUNTING_QUESTS[v][3]) == mobId)
                return World.world.getMonstre(mobId).getGfxId();
        return -1;
    }

    public static int getItemByHuntMob(int mobId) {
        for (int v = 0; v < HUNTING_QUESTS.length; v++)
            if (Integer.parseInt(HUNTING_QUESTS[v][3]) == mobId)
                return Integer.parseInt(HUNTING_QUESTS[v][4]);
        return -1;
    }

    public static int getItemByMobSkin(int mobSkin) {
        for (int v = 0; v < HUNTING_QUESTS.length; v++)
            if (World.world.getMonstre(Integer.parseInt(HUNTING_QUESTS[v][3])) != null
                    && World.world.getMonstre(Integer.parseInt(HUNTING_QUESTS[v][3])).getGfxId() == mobSkin)
                return Integer.parseInt(HUNTING_QUESTS[v][4]);
        return -1;
    }

    public static String getDocNameByBornePos(int borneId, int cellid) {
        for (int v = 0; v < HUNTING_QUESTS.length; v++)
            if (Integer.parseInt(HUNTING_QUESTS[v][0]) == borneId
                    && Integer.parseInt(HUNTING_QUESTS[v][1]) == cellid)
                return HUNTING_QUESTS[v][2];
        return "";
    }
    public static int getEffectSetClasse(int effectID)
    {
        for(int i = 0; i < BUFF_SET_CLASSE.length; i++)
        {
            if(BUFF_SET_CLASSE[i] == effectID)
            {
                return BUFF_SET_CLASSE[i];
            }
            else
            {
                return 0;
            }
        }
        return 0;
    }

    public static short getClassStatueMap(int classID) {
        short pos = 10298;
        switch (classID) {
            case 1:
                return 7398;
            case 2:
                return 7545;
            case 3:
                return 7442;
            case 4:
                return 7392;
            case 5:
                return 7332;
            case 6:
                return 7446;
            case 7:
                return 7361;
            case 8:
                return 7427;
            case 9:
                return 7378;
            case 10:
                return 7395;
            case 11:
                return 7336;
            case 12:
                return 8035;
            case 13:
                return 7427;
        }
        return pos;
    }

    public static int getClassStatueCell(int classID) {
        int pos = 314;
        switch (classID) {
            case 1:
                return 299;
            case 2:
                return 311;
            case 3:
                return 255;
            case 4:
                return 282;
            case 5:
                return 326;
            case 6:
                return 300;
            case 7:
                return 207;
            case 8:
                return 282;
            case 9:
                return 368;
            case 10:
                return 370;
            case 11:
                return 197;
            case 12:
                return 384;
            case 13:
                return 282;
        }
        return pos;
    }

    public static short getStartMap(int classID) {
        short pos = 10298;
        switch (classID) {
            case Constant.CLASS_FECA:
                pos = 10300;
                break;
            case Constant.CLASS_OSAMODAS:
                pos = 10284;
                break;
            case Constant.CLASS_ENUTROF:
                pos = 10299;
                break;
            case Constant.CLASS_SRAM:
                pos = 10285;
                break;
            case Constant.CLASS_XELOR:
                pos = 10298;
                break;
            case Constant.CLASS_ECAFLIP:
                pos = 10276;
                break;
            case Constant.CLASS_ENIRIPSA:
                pos = 10283;
                break;
            case Constant.CLASS_IOP:
                pos = 10294;
                break;
            case Constant.CLASS_CRA:
                pos = 10292;
                break;
            case Constant.CLASS_SADIDA:
                pos = 10279;
                break;
            case Constant.CLASS_SACRIEUR:
                pos = 10296;
                break;
            case Constant.CLASS_PANDAWA:
                pos = 10289;
                break;
        }

        return pos;
    }

    public static int getStartCell(int classID) {
        int pos = 314;
        switch (classID) {
            case Constant.CLASS_FECA:
                pos = 323;
                break;
            case Constant.CLASS_OSAMODAS:
                pos = 372;
                break;
            case Constant.CLASS_ENUTROF:
                pos = 271;
                break;
            case Constant.CLASS_SRAM:
                pos = 263;
                break;
            case Constant.CLASS_XELOR:
                pos = 300;
                break;
            case Constant.CLASS_ECAFLIP:
                pos = 296;
                break;
            case Constant.CLASS_ENIRIPSA:
                pos = 299;
                break;
            case Constant.CLASS_IOP:
                pos = 280;
                break;
            case Constant.CLASS_CRA:
                pos = 284;
                break;
            case Constant.CLASS_SADIDA:
                pos = 254;
                break;
            case Constant.CLASS_SACRIEUR:
                pos = 243;
                break;
            case Constant.CLASS_PANDAWA:
                pos = 236;
                break;
        }
        return pos;
    }

    public static HashMap<Integer, Character> getStartSortsPlaces(int classID) {
        HashMap<Integer, Character> start = new HashMap<Integer, Character>();
        switch (classID) {
            case CLASS_FECA:
                start.put(3, 'b');//Attaque Naturelle
                start.put(6, 'c');//Armure Terrestre
                start.put(17, 'd');//Glyphe Agressif
                break;
            case CLASS_SRAM:
                start.put(61, 'b');//Sournoiserie
                start.put(72, 'c');//Invisibilit�
                start.put(65, 'd');//Piege sournois
                break;
            case CLASS_ENIRIPSA:
                start.put(125, 'b');//Mot Interdit
                start.put(128, 'c');//Mot de Frayeur
                start.put(121, 'd');//Mot Curatif
                break;
            case CLASS_ECAFLIP:
                start.put(102, 'b');//Pile ou Face
                start.put(103, 'c');//Chance d'ecaflip
                start.put(105, 'd');//Bond du felin
                break;
            case CLASS_CRA:
                start.put(161, 'b');//Fleche Magique
                start.put(169, 'c');//Fleche de Recul
                start.put(164, 'd');//Fleche Empoisonn�e(ex Fleche chercheuse)
                break;
            case CLASS_IOP:
                start.put(143, 'b');//Intimidation
                start.put(141, 'c');//Pression
                start.put(142, 'd');//Bond
                break;
            case CLASS_SADIDA:
                start.put(183, 'b');//Ronce
                start.put(200, 'c');//Poison Paralysant
                start.put(193, 'd');//La bloqueuse
                break;
            case CLASS_OSAMODAS:
                start.put(34, 'b');//Invocation de tofu
                start.put(21, 'c');//Griffe Spectrale
                start.put(23, 'd');//Cri de l'ours
                break;
            case CLASS_XELOR:
                start.put(82, 'b');//Contre
                start.put(81, 'c');//Ralentissement
                start.put(83, 'd');//Aiguille
                break;
            case CLASS_PANDAWA:
                start.put(686, 'b');//Picole
                start.put(692, 'c');//Gueule de bois
                start.put(687, 'd');//Poing enflamm�
                break;
            case CLASS_ENUTROF:
                start.put(51, 'b');//Lancer de Piece
                start.put(43, 'c');//Lancer de Pelle
                start.put(41, 'd');//Sac anim�
                break;
            case CLASS_SACRIEUR:
                start.put(432, 'b');//Pied du Sacrieur
                start.put(431, 'c');//Chatiment Os�
                start.put(434, 'd');//Attirance
                break;
        }
        return start;
    }

    public static HashMap<Integer, SpellGrade> getStartSorts(int classID) {
        HashMap<Integer, SpellGrade> start = new HashMap<Integer, SpellGrade>();
        switch (classID) {
            case CLASS_FECA:
                start.put(3, World.world.getSort(3).getStatsByLevel(1));//Attaque Naturelle
                start.put(6, World.world.getSort(6).getStatsByLevel(1));//Armure Terrestre
                start.put(17, World.world.getSort(17).getStatsByLevel(1));//Glyphe Agressif
                break;
            case CLASS_SRAM:
                start.put(61, World.world.getSort(61).getStatsByLevel(1));//Sournoiserie
                start.put(72, World.world.getSort(72).getStatsByLevel(1));//Invisibilit�
                start.put(65, World.world.getSort(65).getStatsByLevel(1));//Piege sournois
                break;
            case CLASS_ENIRIPSA:
                start.put(125, World.world.getSort(125).getStatsByLevel(1));//Mot Interdit
                start.put(128, World.world.getSort(128).getStatsByLevel(1));//Mot de Frayeur
                start.put(121, World.world.getSort(121).getStatsByLevel(1));//Mot Curatif
                break;
            case CLASS_ECAFLIP:
                start.put(102, World.world.getSort(102).getStatsByLevel(1));//Pile ou Face
                start.put(103, World.world.getSort(103).getStatsByLevel(1));//Chance d'ecaflip
                start.put(105, World.world.getSort(105).getStatsByLevel(1));//Bond du felin
                break;
            case CLASS_CRA:
                start.put(161, World.world.getSort(161).getStatsByLevel(1));//Fleche Magique
                start.put(169, World.world.getSort(169).getStatsByLevel(1));//Fleche de Recul
                start.put(164, World.world.getSort(164).getStatsByLevel(1));//Fleche Empoisonn�e(ex Fleche chercheuse)
                break;
            case CLASS_IOP:
                start.put(143, World.world.getSort(143).getStatsByLevel(1));//Intimidation
                start.put(141, World.world.getSort(141).getStatsByLevel(1));//Pression
                start.put(142, World.world.getSort(142).getStatsByLevel(1));//Bond
                break;
            case CLASS_SADIDA:
                start.put(183, World.world.getSort(183).getStatsByLevel(1));//Ronce
                start.put(200, World.world.getSort(200).getStatsByLevel(1));//Poison Paralysant
                start.put(193, World.world.getSort(193).getStatsByLevel(1));//La bloqueuse
                break;
            case CLASS_OSAMODAS:
                start.put(34, World.world.getSort(34).getStatsByLevel(1));//Invocation de tofu
                start.put(21, World.world.getSort(21).getStatsByLevel(1));//Griffe Spectrale
                start.put(23, World.world.getSort(23).getStatsByLevel(1));//Cri de l'ours
                break;
            case CLASS_XELOR:
                start.put(82, World.world.getSort(82).getStatsByLevel(1));//Contre
                start.put(81, World.world.getSort(81).getStatsByLevel(1));//Ralentissement
                start.put(83, World.world.getSort(83).getStatsByLevel(1));//Aiguille
                break;
            case CLASS_PANDAWA:
                start.put(686, World.world.getSort(686).getStatsByLevel(1));//Picole
                start.put(692, World.world.getSort(692).getStatsByLevel(1));//Gueule de bois
                start.put(687, World.world.getSort(687).getStatsByLevel(1));//Poing enflamm�
                break;
            case CLASS_ENUTROF:
                start.put(51, World.world.getSort(51).getStatsByLevel(1));//Lancer de Piece
                start.put(43, World.world.getSort(43).getStatsByLevel(1));//Lancer de Pelle
                start.put(41, World.world.getSort(41).getStatsByLevel(1));//Sac anim�
                break;
            case CLASS_SACRIEUR:
                start.put(432, World.world.getSort(432).getStatsByLevel(1));//Pied du Sacrieur
                start.put(431, World.world.getSort(431).getStatsByLevel(1));//Chatiment Forc�
                start.put(434, World.world.getSort(434).getStatsByLevel(1));//Attirance
                break;
        }
        return start;
    }

    public static HashMap<Integer, SpellGrade> getSortsByClasse(int classID, int level) {
        HashMap<Integer, SpellGrade> start = new HashMap<Integer, SpellGrade>();
        switch (classID) {
            case CLASS_FECA:
                if(level > 0) {
                    start.put(3, World.world.getSort(3).getStatsByLevel(1));//Attaque Naturelle
                    start.put(17, World.world.getSort(17).getStatsByLevel(1));//Glyphe Agressif
                    start.put(6, World.world.getSort(6).getStatsByLevel(1));//Armure Terrestre
                } else if(level >= 3) {
                    start.put(4, World.world.getSort(4).getStatsByLevel(1));//Renvoie de Sort
                } else if(level >= 6) {
                    start.put(2, World.world.getSort(2).getStatsByLevel(1));//Aveuglement
                } else if(level >= 9) {
                    start.put(1, World.world.getSort(1).getStatsByLevel(1));//Armure Incandescente
                } else if(level >= 13) {
                    start.put(9, World.world.getSort(9).getStatsByLevel(1));//Attaque Nuageuse
                } else if(level >= 17) {
                start.put(18, World.world.getSort(18).getStatsByLevel(1));//Armure Aqueuse
                } else if(level >= 21) {
                start.put(20, World.world.getSort(20).getStatsByLevel(1));//Immunité
                } else if(level >= 26) {
                start.put(14, World.world.getSort(14).getStatsByLevel(1));//Armure Venteuse
                } else if(level >= 31) {
                start.put(19, World.world.getSort(19).getStatsByLevel(1));//Bulle
                } else if(level >= 36) {
                start.put(5, World.world.getSort(5).getStatsByLevel(1));//Trêve
                } else if(level >= 42) {
                start.put(16, World.world.getSort(16).getStatsByLevel(1));//Science du Bâton
                } else if(level >= 48) {
                start.put(8, World.world.getSort(8).getStatsByLevel(1));//Retour du Bâton
                } else if(level >= 54) {
                start.put(12, World.world.getSort(12).getStatsByLevel(1));//Glyphe Aveuglement
                } else if(level >= 60) {
                start.put(11, World.world.getSort(11).getStatsByLevel(1));//Téléportation
                } else if(level >= 70) {
                start.put(10, World.world.getSort(10).getStatsByLevel(1));//Glyphe Enflammée
                } else if(level >= 80) {
                start.put(7, World.world.getSort(7).getStatsByLevel(1));//Bouclier Féca
                } else if(level >= 90) {
                start.put(15, World.world.getSort(15).getStatsByLevel(1));//Glyphe Immobilisation
                } else if(level >= 100) {
                start.put(13, World.world.getSort(13).getStatsByLevel(1));//Glyphe Silence
                } else if(level == 200) {
                start.put(1901, World.world.getSort(1901).getStatsByLevel(1));//Dopeul Féca
                }
                break;
            case CLASS_SRAM:
                if(level > 0) {
                    start.put(65, World.world.getSort(65).getStatsByLevel(1));
                    start.put(72, World.world.getSort(72).getStatsByLevel(1));
                    start.put(61, World.world.getSort(61).getStatsByLevel(1));
                } else if(level >= 3) {
                    start.put(66, World.world.getSort(66).getStatsByLevel(1));
                } else if(level >= 6) {
                    start.put(68, World.world.getSort(68).getStatsByLevel(1));
                } else if(level >= 9) {
                    start.put(63, World.world.getSort(63).getStatsByLevel(1));
                } else if(level >= 13) {
                    start.put(74, World.world.getSort(74).getStatsByLevel(1));
                } else if(level >= 17) {
                    start.put(64, World.world.getSort(64).getStatsByLevel(1));
                } else if(level >= 21) {
                    start.put(79, World.world.getSort(79).getStatsByLevel(1));
                } else if(level >= 26) {
                    start.put(78, World.world.getSort(14).getStatsByLevel(1));
                } else if(level >= 31) {
                    start.put(71, World.world.getSort(71).getStatsByLevel(1));
                } else if(level >= 36) {
                    start.put(62, World.world.getSort(62).getStatsByLevel(1));
                } else if(level >= 42) {
                    start.put(69, World.world.getSort(69).getStatsByLevel(1));
                } else if(level >= 48) {
                    start.put(77, World.world.getSort(77).getStatsByLevel(1));
                } else if(level >= 54) {
                    start.put(73, World.world.getSort(73).getStatsByLevel(1));
                } else if(level >= 60) {
                    start.put(67, World.world.getSort(67).getStatsByLevel(1));
                } else if(level >= 70) {
                    start.put(70, World.world.getSort(70).getStatsByLevel(1));
                } else if(level >= 80) {
                    start.put(75, World.world.getSort(75).getStatsByLevel(1));
                } else if(level >= 90) {
                    start.put(76, World.world.getSort(76).getStatsByLevel(1));
                } else if(level >= 100) {
                    start.put(80, World.world.getSort(80).getStatsByLevel(1));
                } else if(level == 200) {
                    start.put(1904, World.world.getSort(1904).getStatsByLevel(1));
                }
                break;
            case CLASS_ENIRIPSA:
                if(level > 0) {
                    start.put(125, World.world.getSort(125).getStatsByLevel(1));
                    start.put(128, World.world.getSort(128).getStatsByLevel(1));
                    start.put(121, World.world.getSort(121).getStatsByLevel(1));
                } else if(level >= 3) {
                    start.put(124, World.world.getSort(124).getStatsByLevel(1));
                } else if(level >= 6) {
                    start.put(122, World.world.getSort(122).getStatsByLevel(1));
                } else if(level >= 9) {
                    start.put(126, World.world.getSort(126).getStatsByLevel(1));
                } else if(level >= 13) {
                    start.put(127, World.world.getSort(127).getStatsByLevel(1));
                } else if(level >= 17) {
                    start.put(123, World.world.getSort(123).getStatsByLevel(1));
                } else if(level >= 21) {
                    start.put(130, World.world.getSort(130).getStatsByLevel(1));
                } else if(level >= 26) {
                    start.put(131, World.world.getSort(131).getStatsByLevel(1));
                } else if(level >= 31) {
                    start.put(132, World.world.getSort(132).getStatsByLevel(1));
                } else if(level >= 36) {
                    start.put(133, World.world.getSort(133).getStatsByLevel(1));
                } else if(level >= 42) {
                    start.put(134, World.world.getSort(134).getStatsByLevel(1));
                } else if(level >= 48) {
                    start.put(135, World.world.getSort(135).getStatsByLevel(1));
                } else if(level >= 54) {
                    start.put(129, World.world.getSort(129).getStatsByLevel(1));
                } else if(level >= 60) {
                    start.put(136, World.world.getSort(136).getStatsByLevel(1));
                } else if(level >= 70) {
                    start.put(137, World.world.getSort(137).getStatsByLevel(1));
                } else if(level >= 80) {
                    start.put(138, World.world.getSort(138).getStatsByLevel(1));
                } else if(level >= 90) {
                    start.put(139, World.world.getSort(139).getStatsByLevel(1));
                } else if(level >= 100) {
                    start.put(140, World.world.getSort(140).getStatsByLevel(1));
                } else if(level == 200) {
                    start.put(1907, World.world.getSort(1907).getStatsByLevel(1));
                }
                break;
            case CLASS_ECAFLIP:
                if(level > 0) {
                    start.put(102, World.world.getSort(102).getStatsByLevel(1));
                    start.put(103, World.world.getSort(103).getStatsByLevel(1));
                    start.put(105, World.world.getSort(105).getStatsByLevel(1));
                } else if(level >= 3) {
                    start.put(109, World.world.getSort(109).getStatsByLevel(1));
                } else if(level >= 6) {
                    start.put(113, World.world.getSort(113).getStatsByLevel(1));
                } else if(level >= 9) {
                    start.put(111, World.world.getSort(111).getStatsByLevel(1));
                } else if(level >= 13) {
                    start.put(104, World.world.getSort(104).getStatsByLevel(1));
                } else if(level >= 17) {
                    start.put(119, World.world.getSort(119).getStatsByLevel(1));
                } else if(level >= 21) {
                    start.put(101, World.world.getSort(101).getStatsByLevel(1));
                } else if(level >= 26) {
                    start.put(107, World.world.getSort(107).getStatsByLevel(1));
                } else if(level >= 31) {
                    start.put(116, World.world.getSort(116).getStatsByLevel(1));
                } else if(level >= 36) {
                    start.put(106, World.world.getSort(106).getStatsByLevel(1));
                } else if(level >= 42) {
                    start.put(117, World.world.getSort(117).getStatsByLevel(1));
                } else if(level >= 48) {
                    start.put(108, World.world.getSort(108).getStatsByLevel(1));
                } else if(level >= 54) {
                    start.put(115, World.world.getSort(115).getStatsByLevel(1));
                } else if(level >= 60) {
                    start.put(118, World.world.getSort(118).getStatsByLevel(1));
                } else if(level >= 70) {
                    start.put(110, World.world.getSort(110).getStatsByLevel(1));
                } else if(level >= 80) {
                    start.put(112, World.world.getSort(112).getStatsByLevel(1));
                } else if(level >= 90) {
                    start.put(114, World.world.getSort(114).getStatsByLevel(1));
                } else if(level >= 100) {
                    start.put(120, World.world.getSort(120).getStatsByLevel(1));
                } else if(level == 200) {
                    start.put(1906, World.world.getSort(1906).getStatsByLevel(1));
                }
                break;
            case CLASS_CRA:
                if(level > 0) {
                    start.put(169, World.world.getSort(169).getStatsByLevel(1));
                    start.put(164, World.world.getSort(164).getStatsByLevel(1));
                    start.put(161, World.world.getSort(161).getStatsByLevel(1));
                } else if(level >= 3) {
                    start.put(163, World.world.getSort(163).getStatsByLevel(1));
                } else if(level >= 6) {
                    start.put(165, World.world.getSort(165).getStatsByLevel(1));
                } else if(level >= 9) {
                    start.put(172, World.world.getSort(172).getStatsByLevel(1));
                } else if(level >= 13) {
                    start.put(167, World.world.getSort(167).getStatsByLevel(1));
                } else if(level >= 17) {
                    start.put(168, World.world.getSort(168).getStatsByLevel(1));
                } else if(level >= 21) {
                    start.put(162, World.world.getSort(162).getStatsByLevel(1));
                } else if(level >= 26) {
                    start.put(170, World.world.getSort(170).getStatsByLevel(1));
                } else if(level >= 31) {
                    start.put(171, World.world.getSort(171).getStatsByLevel(1));
                } else if(level >= 36) {
                    start.put(166, World.world.getSort(166).getStatsByLevel(1));
                } else if(level >= 42) {
                    start.put(173, World.world.getSort(173).getStatsByLevel(1));
                } else if(level >= 48) {
                    start.put(174, World.world.getSort(174).getStatsByLevel(1));
                } else if(level >= 54) {
                    start.put(175, World.world.getSort(175).getStatsByLevel(1));
                } else if(level >= 60) {
                    start.put(176, World.world.getSort(176).getStatsByLevel(1));
                } else if(level >= 70) {
                    start.put(177, World.world.getSort(177).getStatsByLevel(1));
                } else if(level >= 80) {
                    start.put(178, World.world.getSort(178).getStatsByLevel(1));
                } else if(level >= 90) {
                    start.put(179, World.world.getSort(179).getStatsByLevel(1));
                } else if(level >= 100) {
                    start.put(180, World.world.getSort(180).getStatsByLevel(1));
                } else if(level == 200) {
                    start.put(1909, World.world.getSort(1909).getStatsByLevel(1));
                }
                break;
            case CLASS_IOP:
                if(level > 0) {
                    start.put(142, World.world.getSort(142).getStatsByLevel(1));
                    start.put(143, World.world.getSort(143).getStatsByLevel(1));
                    start.put(141, World.world.getSort(141).getStatsByLevel(1));
                } else if(level >= 3) {
                    start.put(144, World.world.getSort(144).getStatsByLevel(1));
                } else if(level >= 6) {
                    start.put(145, World.world.getSort(145).getStatsByLevel(1));
                } else if(level >= 9) {
                    start.put(146, World.world.getSort(146).getStatsByLevel(1));
                } else if(level >= 13) {
                    start.put(147, World.world.getSort(147).getStatsByLevel(1));
                } else if(level >= 17) {
                    start.put(148, World.world.getSort(148).getStatsByLevel(1));
                } else if(level >= 21) {
                    start.put(154, World.world.getSort(154).getStatsByLevel(1));
                } else if(level >= 26) {
                    start.put(150, World.world.getSort(150).getStatsByLevel(1));
                } else if(level >= 31) {
                    start.put(151, World.world.getSort(151).getStatsByLevel(1));
                } else if(level >= 36) {
                    start.put(155, World.world.getSort(155).getStatsByLevel(1));
                } else if(level >= 42) {
                    start.put(152, World.world.getSort(152).getStatsByLevel(1));
                } else if(level >= 48) {
                    start.put(153, World.world.getSort(153).getStatsByLevel(1));
                } else if(level >= 54) {
                    start.put(149, World.world.getSort(149).getStatsByLevel(1));
                } else if(level >= 60) {
                    start.put(156, World.world.getSort(156).getStatsByLevel(1));
                } else if(level >= 70) {
                    start.put(157, World.world.getSort(157).getStatsByLevel(1));
                } else if(level >= 80) {
                    start.put(158, World.world.getSort(158).getStatsByLevel(1));
                } else if(level >= 90) {
                    start.put(160, World.world.getSort(160).getStatsByLevel(1));
                } else if(level >= 100) {
                    start.put(159, World.world.getSort(159).getStatsByLevel(1));
                } else if(level == 200) {
                    start.put(1908, World.world.getSort(1908).getStatsByLevel(1));
                }
                break;
            case CLASS_SADIDA:
                if(level > 0) {
                    start.put(193, World.world.getSort(193).getStatsByLevel(1));
                    start.put(183, World.world.getSort(183).getStatsByLevel(1));
                    start.put(200, World.world.getSort(200).getStatsByLevel(1));
                } else if(level >= 3) {
                    start.put(198, World.world.getSort(198).getStatsByLevel(1));
                } else if(level >= 6) {
                    start.put(195, World.world.getSort(195).getStatsByLevel(1));
                } else if(level >= 9) {
                    start.put(182, World.world.getSort(182).getStatsByLevel(1));
                } else if(level >= 13) {
                    start.put(192, World.world.getSort(192).getStatsByLevel(1));
                } else if(level >= 17) {
                    start.put(197, World.world.getSort(197).getStatsByLevel(1));
                } else if(level >= 21) {
                    start.put(189, World.world.getSort(189).getStatsByLevel(1));
                } else if(level >= 26) {
                    start.put(181, World.world.getSort(181).getStatsByLevel(1));
                } else if(level >= 31) {
                    start.put(199, World.world.getSort(199).getStatsByLevel(1));
                } else if(level >= 36) {
                    start.put(191, World.world.getSort(191).getStatsByLevel(1));
                } else if(level >= 42) {
                    start.put(186, World.world.getSort(186).getStatsByLevel(1));
                } else if(level >= 48) {
                    start.put(196, World.world.getSort(196).getStatsByLevel(1));
                } else if(level >= 54) {
                    start.put(190, World.world.getSort(190).getStatsByLevel(1));
                } else if(level >= 60) {
                    start.put(194, World.world.getSort(194).getStatsByLevel(1));
                } else if(level >= 70) {
                    start.put(185, World.world.getSort(185).getStatsByLevel(1));
                } else if(level >= 80) {
                    start.put(184, World.world.getSort(184).getStatsByLevel(1));
                } else if(level >= 90) {
                    start.put(188, World.world.getSort(188).getStatsByLevel(1));
                } else if(level >= 100) {
                    start.put(187, World.world.getSort(187).getStatsByLevel(1));
                } else if(level == 200) {
                    start.put(1910, World.world.getSort(1910).getStatsByLevel(1));
                }
                break;
            case CLASS_OSAMODAS:
                if(level > 0) {
                    start.put(23, World.world.getSort(23).getStatsByLevel(1));
                    start.put(34, World.world.getSort(34).getStatsByLevel(1));
                    start.put(21, World.world.getSort(21).getStatsByLevel(1));
                } else if(level >= 3) {
                    start.put(26, World.world.getSort(26).getStatsByLevel(1));
                } else if(level >= 6) {
                    start.put(22, World.world.getSort(22).getStatsByLevel(1));
                } else if(level >= 9) {
                    start.put(35, World.world.getSort(35).getStatsByLevel(1));
                } else if(level >= 13) {
                    start.put(28, World.world.getSort(28).getStatsByLevel(1));
                } else if(level >= 17) {
                    start.put(37, World.world.getSort(37).getStatsByLevel(1));
                } else if(level >= 21) {
                    start.put(30, World.world.getSort(30).getStatsByLevel(1));
                } else if(level >= 26) {
                    start.put(27, World.world.getSort(27).getStatsByLevel(1));
                } else if(level >= 31) {
                    start.put(24, World.world.getSort(24).getStatsByLevel(1));
                } else if(level >= 36) {
                    start.put(33, World.world.getSort(33).getStatsByLevel(1));
                } else if(level >= 42) {
                    start.put(25, World.world.getSort(25).getStatsByLevel(1));
                } else if(level >= 48) {
                    start.put(38, World.world.getSort(38).getStatsByLevel(1));
                } else if(level >= 54) {
                    start.put(36, World.world.getSort(36).getStatsByLevel(1));
                } else if(level >= 60) {
                    start.put(32, World.world.getSort(32).getStatsByLevel(1));
                } else if(level >= 70) {
                    start.put(29, World.world.getSort(29).getStatsByLevel(1));
                } else if(level >= 80) {
                    start.put(39, World.world.getSort(39).getStatsByLevel(1));
                } else if(level >= 90) {
                    start.put(40, World.world.getSort(40).getStatsByLevel(1));
                } else if(level >= 100) {
                    start.put(31, World.world.getSort(31).getStatsByLevel(1));
                } else if(level == 200) {
                    start.put(1902, World.world.getSort(1902).getStatsByLevel(1));
                }
                break;
            case CLASS_XELOR:
                if(level > 0) {
                    start.put(81, World.world.getSort(81).getStatsByLevel(1));
                    start.put(83, World.world.getSort(83).getStatsByLevel(1));
                    start.put(82, World.world.getSort(82).getStatsByLevel(1));
                } else if(level >= 3) {
                    start.put(84, World.world.getSort(84).getStatsByLevel(1));
                } else if(level >= 6) {
                    start.put(100, World.world.getSort(100).getStatsByLevel(1));
                } else if(level >= 9) {
                    start.put(92, World.world.getSort(92).getStatsByLevel(1));
                } else if(level >= 13) {
                    start.put(88, World.world.getSort(88).getStatsByLevel(1));
                } else if(level >= 17) {
                    start.put(93, World.world.getSort(93).getStatsByLevel(1));
                } else if(level >= 21) {
                    start.put(85, World.world.getSort(79).getStatsByLevel(1));
                } else if(level >= 26) {
                    start.put(96, World.world.getSort(96).getStatsByLevel(1));
                } else if(level >= 31) {
                    start.put(98, World.world.getSort(98).getStatsByLevel(1));
                } else if(level >= 36) {
                    start.put(86, World.world.getSort(86).getStatsByLevel(1));
                } else if(level >= 42) {
                    start.put(89, World.world.getSort(89).getStatsByLevel(1));
                } else if(level >= 48) {
                    start.put(90, World.world.getSort(90).getStatsByLevel(1));
                } else if(level >= 54) {
                    start.put(87, World.world.getSort(87).getStatsByLevel(1));
                } else if(level >= 60) {
                    start.put(94, World.world.getSort(94).getStatsByLevel(1));
                } else if(level >= 70) {
                    start.put(99, World.world.getSort(99).getStatsByLevel(1));
                } else if(level >= 80) {
                    start.put(95, World.world.getSort(95).getStatsByLevel(1));
                } else if(level >= 90) {
                    start.put(91, World.world.getSort(91).getStatsByLevel(1));
                } else if(level >= 100) {
                    start.put(97, World.world.getSort(97).getStatsByLevel(1));
                } else if(level == 200) {
                    start.put(1905, World.world.getSort(1905).getStatsByLevel(1));
                }
                break;
            case CLASS_PANDAWA:
                if(level > 0) {
                    start.put(686, World.world.getSort(686).getStatsByLevel(1));
                    start.put(692, World.world.getSort(692).getStatsByLevel(1));
                    start.put(687, World.world.getSort(687).getStatsByLevel(1));
                } else if(level >= 3) {
                    start.put(689, World.world.getSort(689).getStatsByLevel(1));
                } else if(level >= 6) {
                    start.put(690, World.world.getSort(690).getStatsByLevel(1));
                } else if(level >= 9) {
                    start.put(691, World.world.getSort(691).getStatsByLevel(1));
                } else if(level >= 13) {
                    start.put(688, World.world.getSort(688).getStatsByLevel(1));
                } else if(level >= 17) {
                    start.put(693, World.world.getSort(693).getStatsByLevel(1));
                } else if(level >= 21) {
                    start.put(694, World.world.getSort(694).getStatsByLevel(1));
                } else if(level >= 26) {
                    start.put(695, World.world.getSort(695).getStatsByLevel(1));
                } else if(level >= 31) {
                    start.put(696, World.world.getSort(696).getStatsByLevel(1));
                } else if(level >= 36) {
                    start.put(697, World.world.getSort(697).getStatsByLevel(1));
                } else if(level >= 42) {
                    start.put(698, World.world.getSort(698).getStatsByLevel(1));
                } else if(level >= 48) {
                    start.put(699, World.world.getSort(699).getStatsByLevel(1));
                } else if(level >= 54) {
                    start.put(700, World.world.getSort(700).getStatsByLevel(1));
                } else if(level >= 60) {
                    start.put(701, World.world.getSort(701).getStatsByLevel(1));
                } else if(level >= 70) {
                    start.put(702, World.world.getSort(702).getStatsByLevel(1));
                } else if(level >= 80) {
                    start.put(703, World.world.getSort(703).getStatsByLevel(1));
                } else if(level >= 90) {
                    start.put(704, World.world.getSort(704).getStatsByLevel(1));
                } else if(level >= 100) {
                    start.put(705, World.world.getSort(705).getStatsByLevel(1));
                } else if(level == 200) {
                    start.put(1912, World.world.getSort(1912).getStatsByLevel(1));
                }
                break;
            case CLASS_ENUTROF:
                if(level > 0) {
                    start.put(51, World.world.getSort(51).getStatsByLevel(1));
                    start.put(41, World.world.getSort(41).getStatsByLevel(1));
                    start.put(43, World.world.getSort(43).getStatsByLevel(1));
                } else if(level >= 3) {
                    start.put(49, World.world.getSort(49).getStatsByLevel(1));
                } else if(level >= 6) {
                    start.put(42, World.world.getSort(42).getStatsByLevel(1));
                } else if(level >= 9) {
                    start.put(47, World.world.getSort(47).getStatsByLevel(1));
                } else if(level >= 13) {
                    start.put(48, World.world.getSort(48).getStatsByLevel(1));
                } else if(level >= 17) {
                    start.put(45, World.world.getSort(45).getStatsByLevel(1));
                } else if(level >= 21) {
                    start.put(53, World.world.getSort(53).getStatsByLevel(1));
                } else if(level >= 26) {
                    start.put(46, World.world.getSort(46).getStatsByLevel(1));
                } else if(level >= 31) {
                    start.put(52, World.world.getSort(52).getStatsByLevel(1));
                } else if(level >= 36) {
                    start.put(44, World.world.getSort(44).getStatsByLevel(1));
                } else if(level >= 42) {
                    start.put(50, World.world.getSort(50).getStatsByLevel(1));
                } else if(level >= 48) {
                    start.put(54, World.world.getSort(54).getStatsByLevel(1));
                } else if(level >= 54) {
                    start.put(55, World.world.getSort(55).getStatsByLevel(1));
                } else if(level >= 60) {
                    start.put(56, World.world.getSort(56).getStatsByLevel(1));
                } else if(level >= 70) {
                    start.put(58, World.world.getSort(58).getStatsByLevel(1));
                } else if(level >= 80) {
                    start.put(59, World.world.getSort(59).getStatsByLevel(1));
                } else if(level >= 90) {
                    start.put(57, World.world.getSort(57).getStatsByLevel(1));
                } else if(level >= 100) {
                    start.put(60, World.world.getSort(60).getStatsByLevel(1));
                } else if(level == 200) {
                    start.put(1903, World.world.getSort(1903).getStatsByLevel(1));
                }
                break;
            case CLASS_SACRIEUR:
                if(level > 0) {
                    start.put(431, World.world.getSort(431).getStatsByLevel(1));
                    start.put(432, World.world.getSort(432).getStatsByLevel(1));
                    start.put(434, World.world.getSort(434).getStatsByLevel(1));
                } else if(level >= 3) {
                    start.put(444, World.world.getSort(444).getStatsByLevel(1));
                } else if(level >= 6) {
                    start.put(449, World.world.getSort(449).getStatsByLevel(1));
                } else if(level >= 9) {
                    start.put(436, World.world.getSort(436).getStatsByLevel(1));
                } else if(level >= 13) {
                    start.put(437, World.world.getSort(437).getStatsByLevel(1));
                } else if(level >= 17) {
                    start.put(439, World.world.getSort(439).getStatsByLevel(1));
                } else if(level >= 21) {
                    start.put(433, World.world.getSort(433).getStatsByLevel(1));
                } else if(level >= 26) {
                    start.put(443, World.world.getSort(443).getStatsByLevel(1));
                } else if(level >= 31) {
                    start.put(440, World.world.getSort(440).getStatsByLevel(1));
                } else if(level >= 36) {
                    start.put(442, World.world.getSort(442).getStatsByLevel(1));
                } else if(level >= 42) {
                    start.put(441, World.world.getSort(441).getStatsByLevel(1));
                } else if(level >= 48) {
                    start.put(445, World.world.getSort(445).getStatsByLevel(1));
                } else if(level >= 54) {
                    start.put(438, World.world.getSort(438).getStatsByLevel(1));
                } else if(level >= 60) {
                    start.put(446, World.world.getSort(446).getStatsByLevel(1));
                } else if(level >= 70) {
                    start.put(447, World.world.getSort(447).getStatsByLevel(1));
                } else if(level >= 80) {
                    start.put(448, World.world.getSort(448).getStatsByLevel(1));
                } else if(level >= 90) {
                    start.put(435, World.world.getSort(435).getStatsByLevel(1));
                } else if(level >= 100) {
                    start.put(450, World.world.getSort(450).getStatsByLevel(1));
                } else if(level == 200) {
                    start.put(1911, World.world.getSort(1911).getStatsByLevel(1));
                }
                break;
        }
        return start;
    }

    public static int getReqPtsToBoostStatsByClassold(int classID, int statID,
                                                   int val) {
        switch (statID) {
            case 11://Vita
                return 1;
            case 12://Sage
                return 3;
            case 10://Force
                switch (classID) {
                    case CLASS_SACRIEUR:
                        return 3;

                    case CLASS_FECA:
                        if (val < 50)
                            return 2;
                        if (val < 150)
                            return 3;
                        if (val < 250)
                            return 4;
                        return 5;

                    case CLASS_XELOR:
                        if (val < 50)
                            return 2;
                        if (val < 150)
                            return 3;
                        if (val < 250)
                            return 4;
                        return 5;

                    case CLASS_SRAM:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASS_OSAMODAS:
                        if (val < 50)
                            return 2;
                        if (val < 150)
                            return 3;
                        if (val < 250)
                            return 4;
                        return 5;

                    case CLASS_ENIRIPSA:
                        if (val < 50)
                            return 2;
                        if (val < 150)
                            return 3;
                        if (val < 250)
                            return 4;
                        return 5;

                    case CLASS_PANDAWA:
                        if (val < 50)
                            return 1;
                        if (val < 200)
                            return 2;
                        return 3;

                    case CLASS_SADIDA:
                        if (val < 50)
                            return 1;
                        if (val < 250)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASS_CRA:
                        if (val < 50)
                            return 1;
                        if (val < 150)
                            return 2;
                        if (val < 250)
                            return 3;
                        if (val < 350)
                            return 4;
                        return 5;

                    case CLASS_ENUTROF:
                        if (val < 50)
                            return 1;
                        if (val < 150)
                            return 2;
                        if (val < 250)
                            return 3;
                        if (val < 350)
                            return 4;
                        return 5;

                    case CLASS_ECAFLIP:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASS_IOP:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;
                }
                break;
            case 13://Chance
                switch (classID) {
                    case CLASS_FECA:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASS_XELOR:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASS_SACRIEUR:
                        return 3;

                    case CLASS_SRAM:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASS_SADIDA:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASS_PANDAWA:
                        if (val < 50)
                            return 1;
                        if (val < 200)
                            return 2;
                        return 3;

                    case CLASS_IOP:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASS_ENUTROF:
                        if (val < 100)
                            return 1;
                        if (val < 150)
                            return 2;
                        if (val < 230)
                            return 3;
                        if (val < 330)
                            return 4;
                        return 5;

                    case CLASS_OSAMODAS:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASS_ECAFLIP:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASS_ENIRIPSA:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASS_CRA:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;
                }
                break;
            case 14://Agilit�
                switch (classID) {
                    case CLASS_FECA:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASS_XELOR:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASS_SACRIEUR:
                        return 3;

                    case CLASS_SRAM:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASS_SADIDA:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASS_PANDAWA:
                        if (val < 50)
                            return 1;
                        if (val < 200)
                            return 2;
                        return 3;

                    case CLASS_ENIRIPSA:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASS_IOP:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASS_ENUTROF:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASS_ECAFLIP:
                        if (val < 50)
                            return 1;
                        if (val < 100)
                            return 2;
                        if (val < 150)
                            return 3;
                        if (val < 200)
                            return 4;
                        return 5;

                    case CLASS_CRA:
                        if (val < 50)
                            return 1;
                        if (val < 100)
                            return 2;
                        if (val < 150)
                            return 3;
                        if (val < 200)
                            return 4;
                        return 5;

                    case CLASS_OSAMODAS:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;
                }
                break;
            case 15://Intelligence
                switch (classID) {
                    case CLASS_XELOR:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASS_FECA:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASS_SACRIEUR:
                        return 3;

                    case CLASS_SRAM:
                        if (val < 50)
                            return 2;
                        if (val < 150)
                            return 3;
                        if (val < 250)
                            return 4;
                        return 5;

                    case CLASS_SADIDA:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASS_ENUTROF:
                        if (val < 20)
                            return 1;
                        if (val < 60)
                            return 2;
                        if (val < 100)
                            return 3;
                        if (val < 140)
                            return 4;
                        return 5;

                    case CLASS_PANDAWA:
                        if (val < 50)
                            return 1;
                        if (val < 200)
                            return 2;
                        return 3;

                    case CLASS_IOP:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;

                    case CLASS_ENIRIPSA:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASS_CRA:
                        if (val < 50)
                            return 1;
                        if (val < 150)
                            return 2;
                        if (val < 250)
                            return 3;
                        if (val < 350)
                            return 4;
                        return 5;

                    case CLASS_OSAMODAS:
                        if (val < 100)
                            return 1;
                        if (val < 200)
                            return 2;
                        if (val < 300)
                            return 3;
                        if (val < 400)
                            return 4;
                        return 5;

                    case CLASS_ECAFLIP:
                        if (val < 20)
                            return 1;
                        if (val < 40)
                            return 2;
                        if (val < 60)
                            return 3;
                        if (val < 80)
                            return 4;
                        return 5;
                }
                break;
        }
        return 5;
    }

    public static int getReqPtsToBoostStatsByClass(int classID, int statID,
                                                      int val) {
        switch (statID) {
            case 11://Vita
                return 1;
            case 12://Sage
                return 1;
            case 10://Force
                return 1;
            case 13://Chance
                return 1;
            case 14://Agilit�
                return 1;
            case 15://Intelligence
                return 1;
        }
        return 5;
    }

    public static Monster.TipoGrupo getTipoGrupoMob(int id) {
        switch (id) {
            case -1:  return Monster.TipoGrupo.FIJO;
            case 0 : return Monster.TipoGrupo.NORMAL;
            case 1 : return Monster.TipoGrupo.SOLO_UNA_PELEA;
            case 2 : return Monster.TipoGrupo.HASTA_QUE_MUERA;
        }
        return Monster.TipoGrupo.FIJO;
    }


    public static void onLevelUpSpells(Player perso, int lvl) {
        switch (perso.getClasse()) {
            case CLASS_FECA:
                if (lvl == 3)
                    perso.learnSpell(4, 1, true, false, false);//Renvoie de sort
                if (lvl == 6)
                    perso.learnSpell(2, 1, true, false, false);//Aveuglement
                if (lvl == 9)
                    perso.learnSpell(1, 1, true, false, false);//Armure Incandescente
                if (lvl == 13)
                    perso.learnSpell(9, 1, true, false, false);//Attaque nuageuse
                if (lvl == 17)
                    perso.learnSpell(18, 1, true, false, false);//Armure Aqueuse
                if (lvl == 21)
                    perso.learnSpell(20, 1, true, false, false);//Immunit�
                if (lvl == 26)
                    perso.learnSpell(14, 1, true, false, false);//Armure Venteuse
                if (lvl == 31)
                    perso.learnSpell(19, 1, true, false, false);//Bulle
                if (lvl == 36)
                    perso.learnSpell(5, 1, true, false, false);//Tr�ve
                if (lvl == 42)
                    perso.learnSpell(16, 1, true, false, false);//Science du b�ton
                if (lvl == 48)
                    perso.learnSpell(8, 1, true, false, false);// falseur du b�ton
                if (lvl == 54)
                    perso.learnSpell(12, 1, true, false, false);//glyphe d'Aveuglement
                if (lvl == 60)
                    perso.learnSpell(11, 1, true, false, false);//T�l�portation
                if (lvl == 70)
                    perso.learnSpell(10, 1, true, false, false);//Glyphe Enflamm�
                if (lvl == 80)
                    perso.learnSpell(7, 1, true, false, false);//Bouclier F�ca
                if (lvl == 90)
                    perso.learnSpell(15, 1, true, false, false);//Glyphe d'Immobilisation
                if (lvl == 100)
                    perso.learnSpell(13, 1, true, false, false);//Glyphe de Silence
                if (lvl == 200)
                    perso.learnSpell(1901, 1, true, false, false);//Invocation de Dopeul F�ca
                break;

            case CLASS_OSAMODAS:
                if (lvl == 3)
                    perso.learnSpell(26, 1, true, false, false);//B�n�diction Animale
                if (lvl == 6)
                    perso.learnSpell(22, 1, true, false, false);//D�placement F�lin
                if (lvl == 9)
                    perso.learnSpell(35, 1, true, false, false);//Invocation de Bouftou
                if (lvl == 13)
                    perso.learnSpell(28, 1, true, false, false);//Crapaud
                if (lvl == 17)
                    perso.learnSpell(37, 1, true, false, false);//Invocation de Prespic
                if (lvl == 21)
                    perso.learnSpell(30, 1, true, false, false);//Fouet
                if (lvl == 26)
                    perso.learnSpell(27, 1, true, false, false);//Piq�re Motivante
                if (lvl == 31)
                    perso.learnSpell(24, 1, true, false, false);//Corbeau
                if (lvl == 36)
                    perso.learnSpell(33, 1, true, false, false);//Griffe Cinglante
                if (lvl == 42)
                    perso.learnSpell(25, 1, true, false, false);//Soin Animal
                if (lvl == 48)
                    perso.learnSpell(38, 1, true, false, false);//Invocation de Sanglier
                if (lvl == 54)
                    perso.learnSpell(36, 1, true, false, false);//Frappe du Craqueleur
                if (lvl == 60)
                    perso.learnSpell(32, 1, true, false, false);//R�sistance Naturelle
                if (lvl == 70)
                    perso.learnSpell(29, 1, true, false, false);//Crocs du Mulou
                if (lvl == 80)
                    perso.learnSpell(39, 1, true, false, false);//Invocation de Bwork Mage
                if (lvl == 90)
                    perso.learnSpell(40, 1, true, false, false);//Invocation de Craqueleur
                if (lvl == 100)
                    perso.learnSpell(31, 1, true, false, false);//Invocation de Dragonnet Rouge
                if (lvl == 200)
                    perso.learnSpell(1902, 1, true, false, false);//Invocation de Dopeul Osamodas
                break;

            case CLASS_ENUTROF:
                if (lvl == 3)
                    perso.learnSpell(49, 1, true, false, false);//Pelle Fantomatique
                if (lvl == 6)
                    perso.learnSpell(42, 1, true, false, false);//Chance
                if (lvl == 9)
                    perso.learnSpell(47, 1, true, false, false);//Bo�te de Pandore
                if (lvl == 13)
                    perso.learnSpell(48, 1, true, false, false);//Remblai
                if (lvl == 17)
                    perso.learnSpell(45, 1, true, false, false);//Cl� R�ductrice
                if (lvl == 21)
                    perso.learnSpell(53, 1, true, false, false);//Force de l'Age
                if (lvl == 26)
                    perso.learnSpell(46, 1, true, false, false);//D�sinvocation
                if (lvl == 31)
                    perso.learnSpell(52, 1, true, false, false);//Cupidit�
                if (lvl == 36)
                    perso.learnSpell(44, 1, true, false, false);//Roulage de Pelle
                if (lvl == 42)
                    perso.learnSpell(50, 1, true, false, false);//Maladresse
                if (lvl == 48)
                    perso.learnSpell(54, 1, true, false, false);//Maladresse de Masse
                if (lvl == 54)
                    perso.learnSpell(55, 1, true, false, false);//Acc�l�ration
                if (lvl == 60)
                    perso.learnSpell(56, 1, true, false, false);//Pelle du Jugement
                if (lvl == 70)
                    perso.learnSpell(58, 1, true, false, false);//Pelle Massacrante
                if (lvl == 80)
                    perso.learnSpell(59, 1, true, false, false);//Corruption
                if (lvl == 90)
                    perso.learnSpell(57, 1, true, false, false);//Pelle Anim�e
                if (lvl == 100)
                    perso.learnSpell(60, 1, true, false, false);//Coffre Anim�
                if (lvl == 200)
                    perso.learnSpell(1903, 1, true, false, false);//Invocation de Dopeul Enutrof
                break;

            case CLASS_SRAM:
                if (lvl == 3)
                    perso.learnSpell(66, 1, true, false, false);//Poison insidieux
                if (lvl == 6)
                    perso.learnSpell(68, 1, true, false, false);//Fourvoiement
                if (lvl == 9)
                    perso.learnSpell(63, 1, true, false, false);//Coup Sournois
                if (lvl == 13)
                    perso.learnSpell(74, 1, true, false, false);//Double
                if (lvl == 17)
                    perso.learnSpell(64, 1, true, false, false);//Rep�rage
                if (lvl == 21)
                    perso.learnSpell(79, 1, true, false, false);//Pi�ge de Masse
                if (lvl == 26)
                    perso.learnSpell(78, 1, true, false, false);//Invisibilit� d'Autrui
                if (lvl == 31)
                    perso.learnSpell(71, 1, true, false, false);//Pi�ge Empoisonn�
                if (lvl == 36)
                    perso.learnSpell(62, 1, true, false, false);//Concentration de Chakra
                if (lvl == 42)
                    perso.learnSpell(69, 1, true, false, false);//Pi�ge d'Immobilisation
                if (lvl == 48)
                    perso.learnSpell(77, 1, true, false, false);//Pi�ge de Silence
                if (lvl == 54)
                    perso.learnSpell(73, 1, true, false, false);//Pi�ge r�pulsif
                if (lvl == 60)
                    perso.learnSpell(67, 1, true, false, false);//Peur
                if (lvl == 70)
                    perso.learnSpell(70, 1, true, false, false);//Arnaque
                if (lvl == 80)
                    perso.learnSpell(75, 1, true, false, false);//Pulsion de Chakra
                if (lvl == 90)
                    perso.learnSpell(76, 1, true, false, false);//Attaque Mortelle
                if (lvl == 100)
                    perso.learnSpell(80, 1, true, false, false);//Pi�ge Mortel
                if (lvl == 200)
                    perso.learnSpell(1904, 1, true, false, false);//Invocation de Dopeul Sram
                break;

            case CLASS_XELOR:
                if (lvl == 3)
                    perso.learnSpell(84, 1, true, false, false);//Gelure
                if (lvl == 6)
                    perso.learnSpell(100, 1, true, false, false);//Sablier de X�lor
                if (lvl == 9)
                    perso.learnSpell(92, 1, true, false, false);//Rayon Obscur
                if (lvl == 13)
                    perso.learnSpell(88, 1, true, false, false);//T�l�portation
                if (lvl == 17)
                    perso.learnSpell(93, 1, true, false, false);//Fl�trissement
                if (lvl == 21)
                    perso.learnSpell(85, 1, true, false, false);//Flou
                if (lvl == 26)
                    perso.learnSpell(96, 1, true, false, false);//Poussi�re Temporelle
                if (lvl == 31)
                    perso.learnSpell(98, 1, true, false, false);//Vol du Temps
                if (lvl == 36)
                    perso.learnSpell(86, 1, true, false, false);//Aiguille Chercheuse
                if (lvl == 42)
                    perso.learnSpell(89, 1, true, false, false);//D�vouement
                if (lvl == 48)
                    perso.learnSpell(90, 1, true, false, false);//Fuite
                if (lvl == 54)
                    perso.learnSpell(87, 1, true, false, false);//D�motivation
                if (lvl == 60)
                    perso.learnSpell(94, 1, true, false, false);//Protection Aveuglante
                if (lvl == 70)
                    perso.learnSpell(99, 1, true, false, false);//Momification
                if (lvl == 80)
                    perso.learnSpell(95, 1, true, false, false);//Horloge
                if (lvl == 90)
                    perso.learnSpell(91, 1, true, false, false);//Frappe de X�lor
                if (lvl == 100)
                    perso.learnSpell(97, 1, true, false, false);//Cadran de X�lor
                if (lvl == 200)
                    perso.learnSpell(1905, 1, true, false, false);//Invocation de Dopeul X�lor
                break;

            case CLASS_ECAFLIP:
                if (lvl == 3)
                    perso.learnSpell(109, 1, true, false, false);//Bluff
                if (lvl == 6)
                    perso.learnSpell(113, 1, true, false, false);//Perception
                if (lvl == 9)
                    perso.learnSpell(111, 1, true, false, false);//Contrecoup
                if (lvl == 13)
                    perso.learnSpell(104, 1, true, false, false);//Tr�fle
                if (lvl == 17)
                    perso.learnSpell(119, 1, true, false, false);//Tout ou rien
                if (lvl == 21)
                    perso.learnSpell(101, 1, true, false, false);//Roulette
                if (lvl == 26)
                    perso.learnSpell(107, 1, true, false, false);//Topkaj
                if (lvl == 31)
                    perso.learnSpell(116, 1, true, false, false);//Langue R�peuse
                if (lvl == 36)
                    perso.learnSpell(106, 1, true, false, false);//Roue de la Fortune
                if (lvl == 42)
                    perso.learnSpell(117, 1, true, false, false);//Griffe Invocatrice
                if (lvl == 48)
                    perso.learnSpell(108, 1, true, false, false);//Esprit F�lin
                if (lvl == 54)
                    perso.learnSpell(115, 1, true, false, false);//Odorat
                if (lvl == 60)
                    perso.learnSpell(118, 1, true, false, false);//R�flexes
                if (lvl == 70)
                    perso.learnSpell(110, 1, true, false, false);//Griffe Joueuse
                if (lvl == 80)
                    perso.learnSpell(112, 1, true, false, false);//Griffe de Ceangal
                if (lvl == 90)
                    perso.learnSpell(114, 1, true, false, false);//Rekop
                if (lvl == 100)
                    perso.learnSpell(120, 1, true, false, false);//Destin d'Ecaflip
                if (lvl == 200)
                    perso.learnSpell(1906, 1, true, false, false);//Invocation de Dopeul Ecaflip
                break;

            case CLASS_ENIRIPSA:
                if (lvl == 3)
                    perso.learnSpell(124, 1, true, false, false);//Mot Soignant
                if (lvl == 6)
                    perso.learnSpell(122, 1, true, false, false);//Mot Blessant
                if (lvl == 9)
                    perso.learnSpell(126, 1, true, false, false);//Mot Stimulant
                if (lvl == 13)
                    perso.learnSpell(127, 1, true, false, false);//Mot de Pr�vention
                if (lvl == 17)
                    perso.learnSpell(123, 1, true, false, false);//Mot Drainant
                if (lvl == 21)
                    perso.learnSpell(130, 1, true, false, false);//Mot Revitalisant
                if (lvl == 26)
                    perso.learnSpell(131, 1, true, false, false);//Mot de R�g�n�ration
                if (lvl == 31)
                    perso.learnSpell(132, 1, true, false, false);//Mot d'Epine
                if (lvl == 36)
                    perso.learnSpell(133, 1, true, false, false);//Mot de Jouvence
                if (lvl == 42)
                    perso.learnSpell(134, 1, true, false, false);//Mot Vampirique
                if (lvl == 48)
                    perso.learnSpell(135, 1, true, false, false);//Mot de Sacrifice
                if (lvl == 54)
                    perso.learnSpell(129, 1, true, false, false);//Mot d'Amiti�
                if (lvl == 60)
                    perso.learnSpell(136, 1, true, false, false);//Mot d'Immobilisation
                if (lvl == 70)
                    perso.learnSpell(137, 1, true, false, false);//Mot d'Envol
                if (lvl == 80)
                    perso.learnSpell(138, 1, true, false, false);//Mot de Silence
                if (lvl == 90)
                    perso.learnSpell(139, 1, true, false, false);//Mot d'Altruisme
                if (lvl == 100)
                    perso.learnSpell(140, 1, true, false, false);//Mot de Reconstitution
                if (lvl == 200)
                    perso.learnSpell(1907, 1, true, false, false);//Invocation de Dopeul Eniripsa
                break;

            case CLASS_IOP:
                if (lvl == 3)
                    perso.learnSpell(144, 1, true, false, false);//Compulsion
                if (lvl == 6)
                    perso.learnSpell(145, 1, true, false, false);//Ep�e Divine
                if (lvl == 9)
                    perso.learnSpell(146, 1, true, false, false);//Ep�e du Destin
                if (lvl == 13)
                    perso.learnSpell(147, 1, true, false, false);//Guide de Bravoure
                if (lvl == 17)
                    perso.learnSpell(148, 1, true, false, false);//Amplification
                if (lvl == 21)
                    perso.learnSpell(154, 1, true, false, false);//Ep�e Destructrice
                if (lvl == 26)
                    perso.learnSpell(150, 1, true, false, false);//Couper
                if (lvl == 31)
                    perso.learnSpell(151, 1, true, false, false);//Souffle
                if (lvl == 36)
                    perso.learnSpell(155, 1, true, false, false);//Vitalit�
                if (lvl == 42)
                    perso.learnSpell(152, 1, true, false, false);//Ep�e du Jugement
                if (lvl == 48)
                    perso.learnSpell(153, 1, true, false, false);//Puissance
                if (lvl == 54)
                    perso.learnSpell(149, 1, true, false, false);//Mutilation
                if (lvl == 60)
                    perso.learnSpell(156, 1, true, false, false);//Temp�te de Puissance
                if (lvl == 70)
                    perso.learnSpell(157, 1, true, false, false);//Ep�e C�leste
                if (lvl == 80)
                    perso.learnSpell(158, 1, true, false, false);//Concentration
                if (lvl == 90)
                    perso.learnSpell(160, 1, true, false, false);//Ep�e de Iop
                if (lvl == 100)
                    perso.learnSpell(159, 1, true, false, false);//Col�re de Iop
                if (lvl == 200)
                    perso.learnSpell(1908, 1, true, false, false);//Invocation de Dopeul Iop
                break;

            case CLASS_CRA:
                if (lvl == 3)
                    perso.learnSpell(163, 1, true, false, false);//Fl�che Glac�e
                if (lvl == 6)
                    perso.learnSpell(165, 1, true, false, false);//Fl�che enflamm�e
                if (lvl == 9)
                    perso.learnSpell(172, 1, true, false, false);//Tir Eloign�
                if (lvl == 13)
                    perso.learnSpell(167, 1, true, false, false);//Fl�che d'Expiation
                if (lvl == 17)
                    perso.learnSpell(168, 1, true, false, false);//Oeil de Taupe
                if (lvl == 21)
                    perso.learnSpell(162, 1, true, false, false);//Tir Critique
                if (lvl == 26)
                    perso.learnSpell(170, 1, true, false, false);//Fl�che d'Immobilisation
                if (lvl == 31)
                    perso.learnSpell(171, 1, true, false, false);//Fl�che Punitive
                if (lvl == 36)
                    perso.learnSpell(166, 1, true, false, false);//Tir Puissant
                if (lvl == 42)
                    perso.learnSpell(173, 1, true, false, false);//Fl�che Harcelante
                if (lvl == 48)
                    perso.learnSpell(174, 1, true, false, false);//Fl�che Cinglante
                if (lvl == 54)
                    perso.learnSpell(176, 1, true, false, false);//Fl�che Pers�cutrice
                if (lvl == 60)
                    perso.learnSpell(175, 1, true, false, false);//Fl�che Destructrice
                if (lvl == 70)
                    perso.learnSpell(178, 1, true, false, false);//Fl�che Absorbante
                if (lvl == 80)
                    perso.learnSpell(177, 1, true, false, false);//Fl�che Ralentissante
                if (lvl == 90)
                    perso.learnSpell(179, 1, true, false, false);//Fl�che Explosive
                if (lvl == 100)
                    perso.learnSpell(180, 1, true, false, false);//Ma�trise de l'Arc
                if (lvl == 200)
                    perso.learnSpell(1909, 1, true, false, false);//Invocation de Dopeul Cra
                break;

            case CLASS_SADIDA:
                if (lvl == 3)
                    perso.learnSpell(198, 1, true, false, false);//Sacrifice Poupesque
                if (lvl == 6)
                    perso.learnSpell(195, 1, true, false, false);//Larme
                if (lvl == 9)
                    perso.learnSpell(182, 1, true, false, false);//Invocation de la Folle
                if (lvl == 13)
                    perso.learnSpell(192, 1, true, false, false);//Ronce Apaisante
                if (lvl == 17)
                    perso.learnSpell(197, 1, true, false, false);//Puissance Sylvestre
                if (lvl == 21)
                    perso.learnSpell(189, 1, true, false, false);//Invocation de la Sacrifi�e
                if (lvl == 26)
                    perso.learnSpell(181, 1, true, false, false);//Tremblement
                if (lvl == 31)
                    perso.learnSpell(199, 1, true, false, false);//Connaissance des Poup�es
                if (lvl == 36)
                    perso.learnSpell(191, 1, true, false, false);//Ronce Multiples
                if (lvl == 42)
                    perso.learnSpell(186, 1, true, false, false);//Arbre
                if (lvl == 48)
                    perso.learnSpell(196, 1, true, false, false);//Vent Empoisonn�
                if (lvl == 54)
                    perso.learnSpell(190, 1, true, false, false);//Invocation de la Gonflable
                if (lvl == 60)
                    perso.learnSpell(194, 1, true, false, false);//Ronces Agressives
                if (lvl == 70)
                    perso.learnSpell(185, 1, true, false, false);//Herbe Folle
                if (lvl == 80)
                    perso.learnSpell(184, 1, true, false, false);//Feu de Brousse
                if (lvl == 90)
                    perso.learnSpell(188, 1, true, false, false);//Ronce Insolente
                if (lvl == 100)
                    perso.learnSpell(187, 1, true, false, false);//Invocation de la Surpuissante
                if (lvl == 200)
                    perso.learnSpell(1910, 1, true, false, false);//Invocation de Dopeul Sadida
                break;

            case CLASS_SACRIEUR:
                if (lvl == 3)
                    perso.learnSpell(444, 1, true, false, false);//D�robade
                if (lvl == 6)
                    perso.learnSpell(449, 1, true, false, false);//D�tour
                if (lvl == 9)
                    perso.learnSpell(436, 1, true, false, false);//Assaut
                if (lvl == 13)
                    perso.learnSpell(437, 1, true, false, false);//Ch�timent Agile
                if (lvl == 17)
                    perso.learnSpell(439, 1, true, false, false);//Dissolution
                if (lvl == 21)
                    perso.learnSpell(433, 1, true, false, false);//Ch�timent Os�
                if (lvl == 26)
                    perso.learnSpell(443, 1, true, false, false);//Ch�timent Spirituel
                if (lvl == 31)
                    perso.learnSpell(440, 1, true, false, false);//Sacrifice
                if (lvl == 36)
                    perso.learnSpell(442, 1, true, false, false);//Absorption
                if (lvl == 42)
                    perso.learnSpell(441, 1, true, false, false);//Ch�timent Vilatesque
                if (lvl == 48)
                    perso.learnSpell(445, 1, true, false, false);//Coop�ration
                if (lvl == 54)
                    perso.learnSpell(438, 1, true, false, false);//Transposition
                if (lvl == 60)
                    perso.learnSpell(446, 1, true, false, false);//Punition
                if (lvl == 70)
                    perso.learnSpell(447, 1, true, false, false);//Furie
                if (lvl == 80)
                    perso.learnSpell(448, 1, true, false, false);//Ep�e Volante
                if (lvl == 90)
                    perso.learnSpell(435, 1, true, false, false);//Tansfert de Vie
                if (lvl == 100)
                    perso.learnSpell(450, 1, true, false, false);//Folie Sanguinaire
                if (lvl == 200)
                    perso.learnSpell(1911, 1, true, false, false);//Invocation de Dopeul Sacrieur
                break;

            case CLASS_PANDAWA:
                if (lvl == 3)
                    perso.learnSpell(689, 1, true, false, false);//Epouvante
                if (lvl == 6)
                    perso.learnSpell(690, 1, true, false, false);//Souffle Alcoolis�
                if (lvl == 9)
                    perso.learnSpell(691, 1, true, false, false);//Vuln�rabilit� Aqueuse
                if (lvl == 13)
                    perso.learnSpell(688, 1, true, false, false);//Vuln�rabilit� Incandescente
                if (lvl == 17)
                    perso.learnSpell(693, 1, true, false, false);//Karcham
                if (lvl == 21)
                    perso.learnSpell(694, 1, true, false, false);//Vuln�rabilit� Venteuse
                if (lvl == 26)
                    perso.learnSpell(695, 1, true, false, false);//Stabilisation
                if (lvl == 31)
                    perso.learnSpell(696, 1, true, false, false);//Chamrak
                if (lvl == 36)
                    perso.learnSpell(697, 1, true, false, false);//Vuln�rabilit� Terrestre
                if (lvl == 42)
                    perso.learnSpell(698, 1, true, false, false);//Souillure
                if (lvl == 48)
                    perso.learnSpell(699, 1, true, false, false);//Lait de Bambou
                if (lvl == 54)
                    perso.learnSpell(700, 1, true, false, false);//Vague � Lame
                if (lvl == 60)
                    perso.learnSpell(701, 1, true, false, false);//Col�re de Zato�shwan
                if (lvl == 70)
                    perso.learnSpell(702, 1, true, false, false);//Flasque Explosive
                if (lvl == 80)
                    perso.learnSpell(703, 1, true, false, false);//Pandatak
                if (lvl == 90)
                    perso.learnSpell(704, 1, true, false, false);//Pandanlku
                if (lvl == 100)
                    perso.learnSpell(705, 1, true, false, false);//Lien Spiritueux
                if (lvl == 200)
                    perso.learnSpell(1912, 1, true, false, false);//Invocation de Dopeul Pandawa
                break;
        }
    }

    public static Stats getMountStats(int color, int lvl) {
        Stats stats = new Stats();
        switch (color) {
            //Amande sauvage
            case 1:
                break;
            //Ebene
            case 3:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_AGIL, (int) (lvl / 1.25));//100/1.25 = 80
                break;
            //Rousse |
            case 10:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl); //100/1 = 100
                break;
            //Amande
            case 20:
                stats.addOneStat(EffectConstant.STATS_ADD_INIT, lvl * 10); // 100*10 = 1000
                break;
            //Dor�e
            case 18:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_SAGE, (int) (lvl / 2.50)); // 100/2.50 = 40
                break;
            //Rousse-Amande
            case 38:
                stats.addOneStat(EffectConstant.STATS_ADD_INIT, lvl * 5); // 100*5 = 500
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_CREATURE, lvl / 50); // 100/50 = 2
                break;
            //Rousse-Dor�e
            case 46:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_SAGE, lvl / 4); //100/4 = 25
                break;
            //Amande-Dor�e
            case 33:
                stats.addOneStat(EffectConstant.STATS_ADD_INIT, lvl * 5);
                stats.addOneStat(EffectConstant.STATS_ADD_SAGE, lvl / 4);
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_CREATURE, lvl / 100); // 100/100 = 1
                break;
            //Indigo |
            case 17:
                stats.addOneStat(EffectConstant.STATS_ADD_CHAN, (int) (lvl / 1.25));
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl / 2);
                break;
            //Rousse-Indigo
            case 62:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, (int) (lvl * 1.50)); // 100*1.50 = 150
                stats.addOneStat(EffectConstant.STATS_ADD_CHAN, (int) (lvl / 1.65));
                break;
            //Rousse-Eb�ne
            case 12:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, (int) (lvl * 1.50));
                stats.addOneStat(EffectConstant.STATS_ADD_AGIL, (int) (lvl / 1.65));
                break;
            //Amande-Indigo
            case 36:
                stats.addOneStat(EffectConstant.STATS_ADD_INIT, lvl * 5);
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_CHAN, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_CREATURE, lvl / 100);
                break;
            //Pourpre | Stade 4
            case 19:
                stats.addOneStat(EffectConstant.STATS_ADD_FORC, (int) (lvl / 1.25));
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl / 2);
                break;
            //Orchid�e
            case 22:
                stats.addOneStat(EffectConstant.STATS_ADD_INTE, (int) (lvl / 1.25));
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl / 2);
                break;
            //Dor�e-Orchid�e |
            case 48:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, (lvl));
                stats.addOneStat(EffectConstant.STATS_ADD_SAGE, lvl / 4);
                stats.addOneStat(EffectConstant.STATS_ADD_INTE, (int) (lvl / 1.65));
                break;
            //Indigo-Pourpre
            case 65:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, (lvl));
                stats.addOneStat(EffectConstant.STATS_ADD_CHAN, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_FORC, lvl / 2);
                break;
            //Ivoire-Orchid�e
            case 67:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, (lvl));
                stats.addOneStat(EffectConstant.STATS_ADD_PERDOM, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_INTE, lvl / 2);
                break;
            //Eb�ne-Pourpre
            case 54:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, (lvl));
                stats.addOneStat(EffectConstant.STATS_ADD_FORC, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_AGIL, lvl / 2);
                break;
            //Eb�ne-Orchid�e
            case 53:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, (lvl));
                stats.addOneStat(EffectConstant.STATS_ADD_AGIL, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_INTE, lvl / 2);
                break;
            //Pourpre-Orchid�e
            case 76:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, (lvl));
                stats.addOneStat(EffectConstant.STATS_ADD_INTE, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_FORC, lvl / 2);
                break;
            //case 37: Amande - Ivoire | Nami-start
            case 37:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, Math.round(lvl * 40/100));
                stats.addOneStat(EffectConstant.STATS_ADD_PERDOM, Math.round(lvl * 40/100));
                stats.addOneStat(EffectConstant.STATS_ADD_INIT, (int) (lvl *5));
                stats.addOneStat(EffectConstant.STATS_CREATURE, lvl / 100);
                break;
            //Amande-Ebene
            case 34:
                stats.addOneStat(EffectConstant.STATS_ADD_INIT, lvl * 5);
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_AGIL, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_CREATURE, lvl / 100);
                break;
            // Amande-Rousse
            case 44:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_SAGE, lvl / 4);
                stats.addOneStat(EffectConstant.STATS_ADD_CHAN, (int) (lvl / 1.65));
                break;
            // Dor�e-Eb�ne
            case 42:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_SAGE, lvl / 4);
                stats.addOneStat(EffectConstant.STATS_ADD_AGIL, (int) (lvl / 1.65));
                break;
            // Indigo-Eb�ne
            case 51:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_CHAN, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_AGIL, lvl / 2);
                break;
            // Rousse-Pourpre
            case 71:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, (int) (lvl * 1.5));
                stats.addOneStat(EffectConstant.STATS_ADD_FORC, (int) (lvl / 1.65));
                break;
            // Rousse-Orchid�e
            case 70:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, (int) (lvl * 1.5));
                stats.addOneStat(EffectConstant.STATS_ADD_INTE, (int) (lvl / 1.65));
                break;
            // Amande-Pourpre
            case 41:
                stats.addOneStat(EffectConstant.STATS_ADD_INIT, lvl * 5);
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_FORC, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_CREATURE, lvl / 100);
                break;
            // Amande-Orchid�e
            case 40:
                stats.addOneStat(EffectConstant.STATS_ADD_INIT, lvl * 5);
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_INTE, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_CREATURE, lvl / 100);
                break;
            // Dor�e-Pourpre
            case 49:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_SAGE, lvl / 4);
                stats.addOneStat(EffectConstant.STATS_ADD_FORC, (int) (lvl / 1.65));
                break;
            // Ivoire
            case 16:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_PERDOM, lvl / 2);
                break;
            // Turquoise
            case 15:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_PROS, (int) (lvl / 1.25));
                break;
            //Rousse-Ivoire
            case 11:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 2); // 100*2 = 200
                stats.addOneStat(EffectConstant.STATS_ADD_PERDOM, (int) (lvl / 2.5)); // = 40
                break;
            //Rousse-Turquoise
            case 69:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(EffectConstant.STATS_ADD_PROS, (int) (lvl / 2.50));
                break;
            //Amande-Turquoise
            case 39:
                stats.addOneStat(EffectConstant.STATS_ADD_INIT, lvl * 5);
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_PROS, (int) (lvl / 2.50));
                stats.addOneStat(EffectConstant.STATS_CREATURE, lvl / 100);
                break;
            //Dor�e-Ivoire
            case 45:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_PERDOM, (int) (lvl / 2.5));
                stats.addOneStat(EffectConstant.STATS_ADD_SAGE, lvl / 4);
                break;
            //Dor�e-Turquoise
            case 47:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_PROS, (int) (lvl / 2.50));
                stats.addOneStat(EffectConstant.STATS_ADD_SAGE, lvl / 4);
                break;
            //Indigo-Ivoire
            case 61:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_CHAN, (int) (lvl / 2.50));
                stats.addOneStat(EffectConstant.STATS_ADD_PERDOM, (int) (lvl / 2.5));
                break;
            //Indigo-Turquoise
            case 63:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_CHAN, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_ADD_PROS, (int) (lvl / 2.5));
                break;
            //Eb�ne-Ivoire
            case 9:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_AGIL, (int) (lvl / 2.50));
                stats.addOneStat(EffectConstant.STATS_ADD_PERDOM, (int) (lvl / 2.5));
                break;
            //Eb�ne-Turquoise
            case 52:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_AGIL, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_ADD_PROS, (int) (lvl / 2.50));
                break;
            //Pourpre-Ivoire
            case 68:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_FORC, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_ADD_PERDOM, (int) (lvl / 2.5));
                break;
            //Pourpre-Turquoise
            case 73:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_FORC, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_ADD_PROS, (int) (lvl / 2.50));
                break;
            //Orchid�e-Turquoise
            case 72:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_INTE, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_ADD_PROS, (int) (lvl / 2.5));
                break;
            //Ivoire-Turquoise
            case 66:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_PERDOM, (int) (lvl / 2.5));
                stats.addOneStat(EffectConstant.STATS_ADD_PROS, (int) (lvl / 2.50));
                break;
            // Emeraude
            case 21:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(EffectConstant.STATS_ADD_PM, lvl / 100);
                break;
            // Prune
            case 23:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 2); // 100*2 = 200
                stats.addOneStat(EffectConstant.STATS_ADD_PO, lvl / 50);
                break;
            //Emeraude-Rousse
            case 57:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 3); // 100*3 = 300
                stats.addOneStat(EffectConstant.STATS_ADD_PM, lvl / 100);
                break;
            //Rousse-Prune
            case 84:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 3);
                stats.addOneStat(EffectConstant.STATS_ADD_PO, lvl / 100);
                break;
            //Amande-Emeraude
            case 35:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_PM, lvl / 100);
                stats.addOneStat(EffectConstant.STATS_CREATURE, lvl / 100);
                stats.addOneStat(EffectConstant.STATS_ADD_INIT, lvl * 5);
                break;
            //Amande-Prune
            case 77:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(EffectConstant.STATS_ADD_INIT, lvl * 5);
                stats.addOneStat(EffectConstant.STATS_ADD_PO, lvl / 100);
                stats.addOneStat(EffectConstant.STATS_CREATURE, lvl / 100);
                break;
            //Dor�e-Emeraude
            case 43:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_SAGE, lvl / 4);
                stats.addOneStat(EffectConstant.STATS_ADD_PM, lvl / 100);
                break;
            //Dor�e-Prune
            case 78:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(EffectConstant.STATS_ADD_SAGE, lvl / 4);
                stats.addOneStat(EffectConstant.STATS_ADD_PO, lvl / 100);
                break;
            //Indigo-Emeraude
            case 55:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_CHAN, (int) (lvl / 3.33));
                stats.addOneStat(EffectConstant.STATS_ADD_PM, lvl / 100);
                break;
            //Indigo-Prune
            case 82:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(EffectConstant.STATS_ADD_CHAN, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_ADD_PO, lvl / 100);
                break;
            //Eb�ne-Emeraude
            case 50:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_AGIL, (int) (lvl / 3.33));
                stats.addOneStat(EffectConstant.STATS_ADD_PM, lvl / 100);
                break;
            //Eb�ne-Prune
            case 79:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(EffectConstant.STATS_ADD_AGIL, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_ADD_PO, lvl / 100);
                break;
            //Pourpre-Emeraude
            case 60:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_FORC, (int) (lvl / 3.33));
                stats.addOneStat(EffectConstant.STATS_ADD_PM, lvl / 100);
                break;
            //Pourpre-Prune
            case 87:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(EffectConstant.STATS_ADD_FORC, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_ADD_PO, lvl / 100);
                break;
            //Orchid�e-Emeraude
            case 59:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_INTE, (int) (lvl / 3.33));
                stats.addOneStat(EffectConstant.STATS_ADD_PM, lvl / 100);
                break;
            //Orchid�e-Prune
            case 86:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(EffectConstant.STATS_ADD_INTE, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_ADD_PO, lvl / 100);
                break;
            //Ivoire-Emeraude
            case 56:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_PERDOM, (int) (lvl / 3.33));
                stats.addOneStat(EffectConstant.STATS_ADD_PM, lvl / 100);
                break;
            //Ivoire-Prune
            case 83:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(EffectConstant.STATS_ADD_PERDOM, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_ADD_PO, lvl / 100);
                break;
            //Turquoise-Emeraude
            case 58:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl);
                stats.addOneStat(EffectConstant.STATS_ADD_PROS, (int) (lvl / 3.33));
                stats.addOneStat(EffectConstant.STATS_ADD_PM, lvl / 100);
                break;
            //Turquoise-Prune
            case 85:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(EffectConstant.STATS_ADD_PROS, (int) (lvl / 1.65));
                stats.addOneStat(EffectConstant.STATS_ADD_PO, lvl / 100);
                break;
            //Emeraude-Prune
            case 80:
                stats.addOneStat(EffectConstant.STATS_ADD_VITA, lvl * 2);
                stats.addOneStat(EffectConstant.STATS_ADD_PM, lvl / 100);
                stats.addOneStat(EffectConstant.STATS_ADD_PO, lvl / 100);
                break;
            //Armure
            case 88:
            case 75:
                stats.addOneStat(EffectConstant.STATS_ADD_PERDOM, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_RP_AIR, lvl / 20);
                stats.addOneStat(EffectConstant.STATS_ADD_RP_EAU, lvl / 20);
                stats.addOneStat(EffectConstant.STATS_ADD_RP_TER, lvl / 20);
                stats.addOneStat(EffectConstant.STATS_ADD_RP_FEU, lvl / 20);
                stats.addOneStat(EffectConstant.STATS_ADD_RP_NEU, lvl / 20);
                break;
            //Tabi
            case 90:
                stats.addOneStat(EffectConstant.STATS_ADD_PERDOM, lvl / 2);
                stats.addOneStat(EffectConstant.STATS_ADD_PA, lvl / 100);
                break;
            //Karnage
            case 91:
                stats.addOneStat(EffectConstant.STATS_ADD_DOMA, lvl / 10);
                stats.addOneStat(EffectConstant.STATS_ADD_PM, lvl / 100);
                break;
        }
        return stats;
    }

    public static ObjectTemplate getParchoTemplateByMountColor(int color) {
        switch (color) {
            //Ammande sauvage
            case 2:
                return World.world.getObjTemplate(7807);
            //Ebene | Page 1
            case 3:
                return World.world.getObjTemplate(7808);
            //Rousse sauvage
            case 4:
                return World.world.getObjTemplate(7809);
            //Ebene-ivoire
            case 9:
                return World.world.getObjTemplate(7810);
            //Rousse
            case 10:
                return World.world.getObjTemplate(7811);
            //Ivoire-Rousse
            case 11:
                return World.world.getObjTemplate(7812);
            //Ebene-rousse
            case 12:
                return World.world.getObjTemplate(7813);
            //Turquoise
            case 15:
                return World.world.getObjTemplate(7814);
            //Ivoire
            case 16:
                return World.world.getObjTemplate(7815);
            //Indigo
            case 17:
                return World.world.getObjTemplate(7816);
            //Dor�e
            case 18:
                return World.world.getObjTemplate(7817);
            //Pourpre
            case 19:
                return World.world.getObjTemplate(7818);
            //Amande
            case 20:
                return World.world.getObjTemplate(7819);
            //Emeraude
            case 21:
                return World.world.getObjTemplate(7820);
            //Orchid�e
            case 22:
                return World.world.getObjTemplate(7821);
            //Prune
            case 23:
                return World.world.getObjTemplate(7822);
            //Amande-Dor�e
            case 33:
                return World.world.getObjTemplate(7823);
            //Amande-Ebene
            case 34:
                return World.world.getObjTemplate(7824);
            //Amande-Emeraude
            case 35:
                return World.world.getObjTemplate(7825);
            //Amande-Indigo
            case 36:
                return World.world.getObjTemplate(7826);
            //Amande-Ivoire
            case 37:
                return World.world.getObjTemplate(7827);
            //Amande-Rousse
            case 38:
                return World.world.getObjTemplate(7828);
            //Amande-Turquoise
            case 39:
                return World.world.getObjTemplate(7829);
            //Amande-Orchid�e
            case 40:
                return World.world.getObjTemplate(7830);
            //Amande-Pourpre
            case 41:
                return World.world.getObjTemplate(7831);
            //Dor�e-Eb�ne
            case 42:
                return World.world.getObjTemplate(7832);
            //Dor�e-Emeraude
            case 43:
                return World.world.getObjTemplate(7833);
            //Dor�e-Indigo
            case 44:
                return World.world.getObjTemplate(7834);
            //Dor�e-Ivoire
            case 45:
                return World.world.getObjTemplate(7835);
            //Dor�e-Rousse | Page 2
            case 46:
                return World.world.getObjTemplate(7836);
            //Dor�e-Turquoise
            case 47:
                return World.world.getObjTemplate(7837);
            //Dor�e-Orchid�e
            case 48:
                return World.world.getObjTemplate(7838);
            //Dor�e-Pourpre
            case 49:
                return World.world.getObjTemplate(7839);
            //Eb�ne-Emeraude
            case 50:
                return World.world.getObjTemplate(7840);
            //Eb�ne-Indigo
            case 51:
                return World.world.getObjTemplate(7841);
            //Eb�ne-Turquoise
            case 52:
                return World.world.getObjTemplate(7842);
            //Eb�ne-Orchid�e
            case 53:
                return World.world.getObjTemplate(7843);
            //Eb�ne-Pourpre
            case 54:
                return World.world.getObjTemplate(7844);
            //Emeraude-Indigo
            case 55:
                return World.world.getObjTemplate(7845);
            //Emeraude-Ivoire
            case 56:
                return World.world.getObjTemplate(7846);
            //Emeraude-Rousse
            case 57:
                return World.world.getObjTemplate(7847);
            //Emeraude-Turquoise
            case 58:
                return World.world.getObjTemplate(7848);
            //Emeraude-Orchid�e
            case 59:
                return World.world.getObjTemplate(7849);
            //Emeraude-Pourpre
            case 60:
                return World.world.getObjTemplate(7850);
            //Indigo-Ivoire
            case 61:
                return World.world.getObjTemplate(7851);
            //Indigo-Rousse
            case 62:
                return World.world.getObjTemplate(7852);
            //Indigo-Turquoise
            case 63:
                return World.world.getObjTemplate(7853);
            //Indigo-Orchid�e
            case 64:
                return World.world.getObjTemplate(7854);
            //Indigo-Pourpre
            case 65:
                return World.world.getObjTemplate(7855);
            //Ivoire-Turquoise
            case 66:
                return World.world.getObjTemplate(7856);
            //Ivoire-Ochid�e
            case 67:
                return World.world.getObjTemplate(7857);
            //Ivoire-Pourpre
            case 68:
                return World.world.getObjTemplate(7858);
            //Turquoise-Rousse
            case 69:
                return World.world.getObjTemplate(7859);
            //Ochid�e-Rousse
            case 70:
                return World.world.getObjTemplate(7860);
            //Pourpre-Rousse
            case 71:
                return World.world.getObjTemplate(7861);
            //Turquoise-Orchid�e
            case 72:
                return World.world.getObjTemplate(7862);
            //Turquoise-Pourpre
            case 73:
                return World.world.getObjTemplate(7863);
            //Dor�e sauvage
            case 74:
                return World.world.getObjTemplate(7864);
            //Squelette
            case 75:
                return World.world.getObjTemplate(7865);
            //Orchid�e-Pourpre
            case 76:
                return World.world.getObjTemplate(7866);
            //Prune-Amande
            case 77:
                return World.world.getObjTemplate(7867);
            //Prune-Dor�e
            case 78:
                return World.world.getObjTemplate(7868);
            //Prune-Eb�ne
            case 79:
                return World.world.getObjTemplate(7869);
            //Prune-Emeraude
            case 80:
                return World.world.getObjTemplate(7870);
            //Prune et Indigo
            case 82:
                return World.world.getObjTemplate(7871);
            //Prune-Ivoire
            case 83:
                return World.world.getObjTemplate(7872);
            //Prune-Rousse
            case 84:
                return World.world.getObjTemplate(7873);
            //Prune-Turquoise
            case 85:
                return World.world.getObjTemplate(7874);
            //Prune-Orchid�e
            case 86:
                return World.world.getObjTemplate(7875);
            //Prune-Pourpre
            case 87:
                return World.world.getObjTemplate(7876);
            //Armure
            case 88:
                return World.world.getObjTemplate(9582);
            //Tabi
            case 90:
                return World.world.getObjTemplate(12780);
            //Karnage
            case 91:
                return World.world.getObjTemplate(12827);

        }
        return null;
    }

    public static int getMountColorByParchoTemplate(int tID) {
        for (int a = 1; a < 100; a++)
            if (getParchoTemplateByMountColor(a) != null)
                if (getParchoTemplateByMountColor(a).getId() == tID)
                    return a;
        return -1;
    }

    public static boolean isValidPlaceForItem(ObjectTemplate template, int place) {
        if (template.getType() == ITEM_TYPE_POISSON && place == ITEM_POS_DRAGODINDE)
            return true;

        switch (template.getType()) {
            case ITEM_TYPE_AMULETTE:
                if (place == ITEM_POS_AMULETTE)
                    return true;
                break;
            case ITEM_TYPE_OBJET_VIVANT:
                if ((template.getId() == 9233) && (place == 7))
                    return true;
                if ((template.getId() == 9234) && (place == 6))
                    return true;
                if ((template.getId() == 9255) && (place == 0))
                    return true;
                if ((template.getId() == 9256)
                        && ((place == 2) || (place == 4)))
                    return true;
                break;
            case ITEM_TYPE_ARME_MAGIQUE: // tourmenteurs
                if (place == 1) // CaC
                    return true;
                break;
            case ITEM_TYPE_ARC:
            case ITEM_TYPE_BAGUETTE:
            case ITEM_TYPE_BATON:
            case ITEM_TYPE_DAGUES:
            case ITEM_TYPE_EPEE:
            case ITEM_TYPE_MARTEAU:
            case ITEM_TYPE_PELLE:
            case ITEM_TYPE_HACHE:
            case ITEM_TYPE_OUTIL:
            case ITEM_TYPE_PIOCHE:
            case ITEM_TYPE_FAUX:
            case ITEM_TYPE_PIERRE_AME:
            case ITEM_TYPE_FILET_CAPTURE:
                if (place == ITEM_POS_ARME)
                    return true;
                break;

            case ITEM_TYPE_ANNEAU:
                if (place == ITEM_POS_ANNEAU1 || place == ITEM_POS_ANNEAU2)
                    return true;
                break;

            case ITEM_TYPE_CEINTURE:
                if (place == ITEM_POS_CEINTURE)
                    return true;
                break;

            case ITEM_TYPE_BOTTES:
                if (place == ITEM_POS_BOTTES)
                    return true;
                break;

            case ITEM_TYPE_COIFFE:
                if (place == ITEM_POS_COIFFE)
                    return true;
                break;

            case ITEM_TYPE_CAPE:
            case ITEM_TYPE_SAC_DOS:
                if (place == ITEM_POS_CAPE)
                    return true;
                break;

            case ITEM_TYPE_FAMILIER:
                if (place == ITEM_POS_FAMILIER)
                    return true;
                break;

            case ITEM_TYPE_DOFUS:
                if (place == ITEM_POS_DOFUS1 || place == ITEM_POS_DOFUS2
                        || place == ITEM_POS_DOFUS3 || place == ITEM_POS_DOFUS4
                        || place == ITEM_POS_DOFUS5 || place == ITEM_POS_DOFUS6)
                    return true;
                break;

            case ITEM_TYPE_BOUCLIER:
                if (place == ITEM_POS_BOUCLIER)
                    return true;
                break;

            //Barre d'objets : Normalement le client bloque les items interdits
            case ITEM_TYPE_POTION:
            case ITEM_TYPE_PARCHO_EXP:
            case ITEM_TYPE_BOOST_FOOD:
            case ITEM_TYPE_PAIN:
            case ITEM_TYPE_BIERE:
            case ITEM_TYPE_POISSON:
            case ITEM_TYPE_BONBON:
            case ITEM_TYPE_COMESTI_POISSON:
            case ITEM_TYPE_VIANDE:
            case ITEM_TYPE_VIANDE_CONSERVEE:
            case ITEM_TYPE_VIANDE_COMESTIBLE:
            case ITEM_TYPE_TEINTURE:
            case ITEM_TYPE_MAITRISE:
            case ITEM_TYPE_BOISSON:
            case ITEM_TYPE_PIERRE_AME_PLEINE:
            case ITEM_TYPE_PIERRE_AME_PLEINE_BOSS:
            case ITEM_TYPE_PIERRE_AME_PLEINE_ARCHI:
            case ITEM_TYPE_PARCHO_RECHERCHE:
            case ITEM_TYPE_CADEAUX:
            case ITEM_TYPE_OBJET_ELEVAGE:
            case ITEM_TYPE_OBJET_UTILISABLE:
            case ITEM_TYPE_PRISME:
            case ITEM_TYPE_FEE_ARTIFICE:
            case ITEM_TYPE_DONS:
            case ITEM_TYPE_PIERRE_MAGIQUE:
                if (place >= 35 && place <= 48)
                    return true;
                break;
        }
        return false;
    }

	/*
     * public static boolean feedMount(int type) { for (Integer feed :
	 * Main.itemFeedMount) { if (type == feed) return true; } return false; }
	 */

    public static void tpCim(int idArea, Player perso) {
        switch (idArea) {
            case 45:
                perso.teleport((short) 10342, 222);
                break;

            case 0:
            case 5:
            case 29:
            case 39:
            case 40:
            case 43:
            case 44:
                perso.teleport((short) 1174, 279);
                break;

            case 3:
            case 4:
            case 6:
            case 18:
            case 25:
            case 27:
            case 41:

            case 42:
                perso.teleport((short) 8534, 196);
                break;

            case 2:
                perso.teleport((short) 420, 408);
                break;

            case 1:
                perso.teleport((short) 844, 370);
                break;

            case 7:
                perso.teleport((short) 4285, 572);
                break;

            case 8:
            case 14:
            case 15:
            case 16:
            case 32:
                perso.teleport((short) 4748, 133);
                break;

            case 11:
            case 12:
            case 13:
            case 33:
                perso.teleport((short) 5719, 196);
                break;

            case 19:
            case 22:
            case 23:
                perso.teleport((short) 7910, 381);
                break;

            case 20:
            case 21:
            case 24:
                perso.teleport((short) 8054, 115);
                break;

            case 28:
            case 34:
            case 35:
            case 36:
                perso.teleport((short) 9231, 257);
                break;

            case 30:
                perso.teleport((short) 9539, 128);
                break;

            case 31:
                if (perso.isGhost())
                    perso.teleport((short) 9558, 268);
                else
                    perso.teleport((short) 9558, 224);
                break;

            case 37:
                perso.teleport((short) 7796, 433);
                break;

            case 46:
                perso.teleport((short) 10422, 327);
                break;
            case 47:
                perso.teleport((short) 10590, 302);
                break;

            case 26:
                perso.teleport((short) 9398, 268);

            default:
                perso.teleport((short) 8534, 196);
                break;
        }
    }

    public static boolean isTaverne(GameMap map) {
        switch (map.getId()) {
            case 7573:
            case 7572:
            case 7574:
            case 465:
            case 463:
            case 6064:
            case 461:
            case 462:
            case 5867:
            case 6197:
            case 6021:
            case 6044:
            case 8196:
            case 6055:
            case 8195:
            case 1905:
            case 1907:
            case 6049:
                return true;
        }
        return false;
    }

    public static int getLevelForChevalier(Player target) {
        int lvl = target.getLevel();
        if (lvl <= 50)
            return 50;
        if ((lvl <= 80) && (lvl > 50))
            return 80;
        if ((lvl <= 110) && (lvl > 80))
            return 110;
        if ((lvl <= 140) && (lvl > 110))
            return 140;
        if ((lvl <= 170) && (lvl > 140))
            return 170;
        if ((lvl <= 500) && (lvl > 170))
            return 200;
        return 200;
    }

    public static String getStatsOfCandy(int id, int turn) {
        String a = World.world.getObjTemplate(id).getStrTemplate();
        a += ",32b#64#0#" + Integer.toHexString(turn) + "#0d0+1;";
        return a;
    }

    public static String getStatsOfMascotte() {
        String a = Integer.toHexString(148) + "#0#0#0#0d0+1,";
        a += "32b#64#0#" + Integer.toHexString(1) + "#0d0+1;";
        return a;
    }

    public static String getStringColorDragodinde(int color) {
        switch (color) {
            case 1: // Dragodinde Amande Sauvage
                return "16772045,-1,16772045";
            case 3: // Dragodinde Ebène
                return "1245184,393216,1245184";
            case 6: // Dragodinde Rousse Sauvage
                return "16747520,-1,16747520";
            case 9: // Dragodinde Ebène et Ivoire
                return "1182992,16777200,16777200";
            case 10: // Dragodinde Rousse
                return "16747520,-1,16747520";
            case 11: // Dragodinde Ivoire et Rousse
                return "16747520,16777200,16777200";
            case 12: // Dragodinde Ebène et Rousse
                return "16747520,1703936,1774084";
            case 15: // Dragodinde Turquoise
                return "4251856,-1,4251856";
            case 16: // Dragodinde Ivoire
                return "16777200,16777200,16777200";
            case 17: // Dragodinde Indigo
                return "4915330,-1,4915330";
            case 18: // Dragodinde Dorée
                return "16766720,16766720,16766720";
            case 19: // Dragodinde Pourpre
                return "14423100,-1,14423100";
            case 20: // Dragodinde Amande
                return "16772045,-1,16772045";
            case 21: // Dragodinde Emeraude
                return "3329330,-1,3329330";
            case 22: // Dragodinde Orchidée
                return "15859954,16777200,15859954";
            case 23: // Dragodinde Prune
                return "14524637,-1,14524637";
            case 33: // Dragodinde Amande et Dorée
                return "16772045,16766720,16766720";
            case 34: // Dragodinde Amande et Ebène
                return "16772045,1245184,1245184";
            case 35: // Dragodinde Amande et Emeraude
                return "16772045,3329330,3329330";
            case 36: // Dragodinde Amande et Indigo
                return "16772045,4915330,4915330";
            case 37: // Dragodinde Amande et Ivoire
                return "16772045,16777200,16777200";
            case 38: // Dragodinde Amande et Rousse
                return "16772045,16747520,16747520";
            case 39: // Dragodinde Amande et Turquoise
                return "16772045,4251856,4251856";
            case 40: // Dragodinde Amande et Orchidée
                return "16772045,15859954,15859954";
            case 41: // Dragodinde Amande et Pourpre
                return "16772045,14423100,14423100";
            case 42: // Dragodinde Dorée et Ebène
                return "1245184,16766720,16766720";
            case 43: // Dragodinde Dorée et Emeraude
                return "16766720,3329330,3329330";
            case 44: // Dragodinde Dorée et Indigo
                return "16766720,4915330,4915330";
            case 45: // Dragodinde Dorée et Ivoire
                return "16766720,16777200,16777200";
            case 46: // Dragodinde Dorée et Rousse
                return "16766720,16747520,16747520";
            case 47: // Dragodinde Dorée et Turquoise
                return "16766720,4251856,4251856";
            case 48: // Dragodinde Dorée et Orchidée
                return "16766720,15859954,15859954";
            case 49: // Dragodinde Dorée et Pourpre
                return "16766720,14423100,14423100";
            case 50: // Dragodinde Ebène et Emeraude
                return "1245184,3329330,3329330";
            case 51: // Dragodinde Ebène et Indigo
                return "4915330,4915330,1245184";
            case 52: // Dragodinde Ebène et Turquoise
                return "1245184,4251856,4251856";
            case 53: // Dragodinde Ebène et Orchidée
                return "15859954,0,0";
            case 54: // Dragodinde Ebène et Pourpre
                return "14423100,14423100,1245184";
            case 55: // Dragodinde Emeraude et Indigo
                return "3329330,4915330,4915330";
            case 56: // Dragodinde Emeraude et Ivoire
                return "3329330,16777200,16777200";
            case 57: // Dragodinde Emeraude et Rousse
                return "3329330,16747520,16747520";
            case 58: // Dragodinde Emeraude et Turquoise
                return "3329330,4251856,4251856";
            case 59: // Dragodinde Emeraude et Orchidée
                return "3329330,15859954,15859954";
            case 60: // Dragodinde Emeraude et Pourpre
                return "3329330,14423100,14423100";
            case 61: // Dragodinde Indigo et Ivoire
                return "4915330,16777200,16777200";
            case 62: // Dragodinde Indigo et Rousse
                return "4915330,16747520,16747520";
            case 63: // Dragodinde Indigo et Turquoise
                return "4915330,4251856,4251856";
            case 64: // Dragodinde Indigo et Orchidée
                return "4915330,15859954,15859954";
            case 65: // Dragodinde Indigo et Pourpre
                return "14423100,4915330,4915330";
            case 66: // Dragodinde Ivoire et Turquoise
                return "16777200,4251856,4251856";
            case 67: // Dragodinde Ivoire et Orchidée
                return "16777200,16731355,16711910";
            case 68: // Dragodinde Ivoire et Pourpre
                return "14423100,16777200,16777200";
            case 69: // Dragodinde Ivoire et Rousse
                return "4251856,16747520,16747520";
            case 70: // Dragodinde Orchidée et Rousse
                return "14315734,16747520,16747520";
            case 71: // Dragodinde Pourpre et Rousse
                return "14423100,16747520,16747520";
            case 72: // Dragodinde Turquoise et Orchidée
                return "15859954,4251856,4251856";
            case 73: // Dragodinde Turquoise et Pourpre
                return "14423100,4251856,4251856";
            case 74: // Dragodinde Dorée et Rousse
                return "16766720,16766720,16766720";
            case 76: // Dragodinde Orchidée et Pourpre
                return "14315734,14423100,14423100";
            case 77: // Dragodinde Prune et Amande
                return "14524637,16772045,16772045";
            case 78: // Dragodinde Prune et Dorée
                return "14524637,16766720,16766720";
            case 79: // Dragodinde Prune et Ebène
                return "14524637,1245184,1245184";
            case 80: // Dragodinde Prune et Emeraude
                return "14524637,3329330,3329330";
            case 82: // Dragodinde Prune et Indigo
                return "14524637,4915330,4915330";
            case 83: // Dragodinde Prune et Ivoire
                return "14524637,16777200,16777200";
            case 84: // Dragodinde Prune et Rousse
                return "14524637,16747520,16747520";
            case 85: // Dragodinde Prune et Turquoise
                return "14524637,4251856,4251856";
            case 86: // Dragodinde Prune et Orchidée
                return "14524637,15859954,15859954";
            case 87: // Dragodinde Prune et Pourpre
                return "14524637,14423100,14423100";
            default:
                return "-1,-1,-1";
        }
    }

    public static int getGeneration(int color) {
        switch (color) {
            case 10: // Rousse
            case 18: // Dorée
            case 20: // Amande
                return 1;
            case 33: // Amande - Dorée
            case 38: // Amande - Rousse
            case 46: // Dorée - Rousse
                return 2;
            case 3: // Ebène
            case 17: // Indigo
                return 3;
            case 62: // Indigo - Rousse
            case 12: // Ebène - Rousse
            case 36: // Amande - Indigo
            case 34: // Amande - Ebène
            case 44: // Dorée - Indigo
            case 42: // Dorée - Ebène
            case 51: // Ebène - Indigo
                return 4;
            case 19: // Purpre
            case 22: // Orchidée
                return 5;
            case 71: // Purpre - Rousse
            case 70: // Orchidée - Rousse
            case 41: // Amande - Purpre
            case 40: // Amande - Orchidée
            case 49: // Dorée - Purpre
            case 48: // Dorée - Orchidée
            case 65: // Indigo - Purpre
            case 64: // Indigo - Orchidée
            case 54: // Ebène - Purpre
            case 53: // Ebène - Orchidée
            case 76: // Orchidée - Purpre
                return 6;
            case 15: // Turquoise
            case 16: // Ivoire
                return 7;
            case 11: // Ivoire - Rousse
            case 69: // Turquoise - Rousse
            case 37: // Amande - Ivoire
            case 39: // Amande - Turquoise
            case 45: // Dorée - Ivoire
            case 47: // Dorée - Turquoise
            case 61: // Indigo - Ivoire
            case 63: // Indigo - Turquoise
            case 9: // Ebène - Ivoire
            case 52: // Ebène - Turquoise
            case 68: // Ivoire - Purpre
            case 73: // Turquoise - Purpre
            case 67: // Ivoire - Orchidée
            case 72: // Orchidée - Turquoise
            case 66: // Ivoire - Turquoise
                return 8;
            case 21: // Emeraude
            case 23: // Prune
                return 9;
            case 57:// Emeraude - Rousse
            case 35: // Amande - Emeraude
            case 43: // Dorée - Emeraude
            case 50: // Ebène - Emeraude
            case 55: // Emeraude - Indigo
            case 56: // Emeraude - Ivoire
            case 58: // Emeraude - Turquoise
            case 59: // Emeraude - Orchidée
            case 60: // Emeraude - Purpre
            case 77: // Prune - Amande
            case 78: // Prune - Dorée
            case 79: // Prune - Ebène
            case 80: // Prune - Emeraude
            case 82: // Prune - Indigo
            case 83: // Prune - Ivoire
            case 84: // Prune - Rousse
            case 85: // Prune - Turquoise
            case 86: // Prune - Orchidée
                return 10;
            default:
                return 1;
        }
    }

    public static int colorToEtable(Player player, Mount mother, Mount father) {
        int color1, color2;
        int A = 0, B = 0, C = 0;

        String[] splitM = mother.getAncestors().split(","), splitF = father.getAncestors().split(",");
        RandomStats<Integer> random = new RandomStats<>();

        short i = 0;
        for(String str : splitM) {
            i++;
            if(str.equals("?")) continue;

            int pct = 1;

            switch(i) {
                case 1: case 2: pct = 25; break;
                case 3: case 4: case 5: case 6: pct = 10;
            }

            random.add(pct, Integer.parseInt(str));
        }

        random.add(random.size() == 0 ? 100 : 33, mother.getColor());
        color1 = random.get();

        random = new RandomStats<>();
        i = 0;
        for(String str : splitF) {
            i++;
            if(str.equals("?")) continue;

            int pct = 1;

            switch(i) {
                case 1: case 2: pct = 25; break;
                case 3: case 4: case 5: case 6: pct = 10;
            }

            random.add(pct, Integer.parseInt(str));
        }

        random.add(random.size() == 0 ? 100 : 33, father.getColor());
        color2 = random.get();

        if(color1 == 75)
            color1 = 10;
        if(color2 == 75)
            color2 = 10;

        if (color1 > color2) {
            A = color2;// moins
            B = color1;// supérieur
        } else if (color1 <= color2) {
            A = color1;// moins
            B = color2;// supérieur
        }
        if (A == 10 && B == 18)
            C = 46; // Rousse y Dorée
        else if (A == 10 && B == 20)
            C = 38; // Rousse y Amande
        else if (A == 18 && B == 20)
            C = 33; // Amande y Dorée
        else if (A == 33 && B == 38)
            C = 17; // Indigo
        else if (A == 33 && B == 46)
            C = 3;// Ebène
        else if (A == 10 && B == 17)
            C = 62; // Rousse e Indigo
        else if (A == 10 && B == 3)
            C = 12; // Ebène y Rousse
        else if (A == 17 && B == 20)
            C = 36; // Amande - Indigo
        else if (A == 3 && B == 20)
            C = 34; // Amande - Ebène
        else if (A == 17 && B == 18)
            C = 44; // Dorée - Indigo
        else if (A == 3 && B == 18)
            C = 42; // Dorée - Ebène
        else if (A == 3 && B == 17)
            C = 51; // Ebène - Indigo
        else if (A == 38 && B == 51)
            C = 19; // Purpre
        else if (A == 46 && B == 51)
            C = 22; // Orchidée
        else if (A == 10 && B == 19)
            C = 71; // Purpre - Rousse
        else if (A == 10 && B == 22)
            C = 70; // Orchidée - Rousse
        else if (A == 19 && B == 20)
            C = 41; // Amande - Purpre
        else if (A == 20 && B == 22)
            C = 40; // Amande - Orchidée
        else if (A == 18 && B == 19)
            C = 49; // Dorée - Purpre
        else if (A == 18 && B == 22)
            C = 48; // Dorée - Orchidée
        else if (A == 17 && B == 19)
            C = 65; // Indigo - Purpre
        else if (A == 17 && B == 22)
            C = 64; // Indigo - Orchidée
        else if (A == 3 && B == 19)
            C = 54; // Ebène - Purpre
        else if (A == 3 && B == 22)
            C = 53; // Ebène - Orchidée
        else if (A == 19 && B == 22)
            C = 76; // Orchidée - Purpre
        else if (A == 53 && B == 76)
            C = 15; // Turquoise
        else if (A == 65 && B == 76)
            C = 16; // Ivoire
        else if (A == 10 && B == 16)
            C = 11; // Ivoire - Rousse
        else if (A == 10 && B == 15)
            C = 69; // Turquoise - Rousse
        else if (A == 16 && B == 20)
            C = 37; // Amande - Ivoire
        else if (A == 15 && B == 20)
            C = 39; // Amande - Turquoise
        else if (A == 16 && B == 18)
            C = 45; // Dorée - Ivoire
        else if (A == 15 && B == 18)
            C = 47; // Dorée - Turquoise
        else if (A == 16 && B == 17)
            C = 61; // Indigo - Ivoire
        else if (A == 15 && B == 17)
            C = 63; // Indigo - Turquoise
        else if (A == 3 && B == 16)
            C = 9; // Ebène - Ivoire
        else if (A == 3 && B == 15)
            C = 52; // Ebène - Turquoise
        else if (A == 16 && B == 19)
            C = 68; // Ivoire - Purpre
        else if (A == 15 && B == 19)
            C = 73; // Turquoise - Purpre
        else if (A == 16 && B == 22)
            C = 67; // Ivoire - Orchidée
        else if (A == 15 && B == 22)
            C = 72; // Orchidée - Turquoise
        else if (A == 15 && B == 16)
            C = 66; // Ivoire - Turquoise
        else if (A == 66 && B == 68)
            C = 21; // Emeraude
        else if (A == 66 && B == 72)
            C = 23; // Prune
        else if (A == 10 && B == 21)
            C = 57;// Emeraude - Rousse
        else if (A == 20 && B == 21)
            C = 35; // Amande - Emeraude
        else if (A == 18 && B == 21)
            C = 43; // Dorée - Emeraude
        else if (A == 3 && B == 21)
            C = 50; // Ebène - Emeraude
        else if (A == 17 && B == 21)
            C = 55; // Emeraude - Indigo
        else if (A == 16 && B == 21)
            C = 56; // Emeraude - Ivoire
        else if (A == 15 && B == 21)
            C = 58; // Emeraude - Turquoise
        else if (A == 21 && B == 22)
            C = 59; // Emeraude - Orchidée
        else if (A == 19 && B == 21)
            C = 60; // Emeraude - Purpre
        else if (A == 20 && B == 23)
            C = 77; // Prune - Amande
        else if (A == 18 && B == 23)
            C = 78; // Prune - Dorée
        else if (A == 3 && B == 23)
            C = 79; // Prune - Ebène
        else if (A == 21 && B == 23)
            C = 80; // Prune - Emeraude
        else if (A == 17 && B == 23)
            C = 82; // Prune - Indigo
        else if (A == 16 && B == 23)
            C = 83; // Prune - Ivoire
        else if (A == 10 && B == 23)
            C = 84; // Prune - Rousse
        else if (A == 15 && B == 23)
            C = 85; // Prune - Turquoise
        else if (A == 22 && B == 23)
            C = 86; // Prune - Orchidée
        else if (A == 19 && B == 23)
            C = 87; // Prune - Purpre
        else if (A == B)
            C = A = B;

        if(C == 0) {

            random = new RandomStats<>();
            i = 0;
            for(String str : splitF) {
                i++;
                if(str.equals("?")) continue;

                int pct = 1;

                switch(i) {
                    case 1: case 2: pct = 25; break;
                    case 3: case 4: case 5: case 6: pct = 10;
                }

                random.add(pct, Integer.parseInt(str));
            }
            i = 0;
            for(String str : splitM) {
                i++;
                if(str.equals("?")) continue;

                int pct = 1;

                switch(i) {
                    case 1: case 2: pct = 25; break;
                    case 3: case 4: case 5: case 6: pct = 10;
                }

                random.add(pct, Integer.parseInt(str));
            }
            C = random.get();
            //player.sendMessage("Merci de crier auprès du staff que C = 0, A = " + A + ", et B = " + B + ". Valeur finale : " + C + ". Message bien évidement sérieux.");

            return C;
        }
        random = new RandomStats<>();
        random.add(33, A);
        random.add(33, B);
        random.add(33, C);
        return random.get();
    }

    public static int getParchoByIdPets(int id) {
        switch (id) {
            case 10802:
                return 10806;
            case 10107:
                return 10135;
            case 10106:
                return 10134;
            case 9795:
                return 9810;
            case 9624:
                return 9685;
            case 9623:
                return 9684;
            case 9620:
                return 9683;
            case 9619:
                return 9682;
            case 9617:
                return 9675;
            case 9594:
                return 9598;
            case 8693:
                return 8707;
            case 8677:
                return 8684;
            case 8561:
                return 8564;
            case 8211:
                return 8544;
            case 8155:
                return 8179;
            case 8154:
                return 8178;
            case 8153:
                return 8175;
            case 8151:
                return 8176;
            case 8000:
                return 8180;
            case 7911:
                return 8526;
            case 7892:
                return 7896;
            case 7891:
                return 7895;
            case 7714:
                return 8708;
            case 7713:
                return 9681;
            case 7712:
                return 9680;
            case 7711:
                return 9679;
            case 7710:
                return 9678;
            case 7709:
                return 9677;
            case 7708:
                return 9676;
            case 7707:
                return 9674;
            case 7706:
                return 8685;
            case 7705:
                return 8889;
            case 7704:
                return 8888;
            case 7703:
                return 8421;
            case 7524:
                return 8887;
            case 7522:
                return 7535;
            case 7520:
                return 7533;
            case 7519:
                return 7534;
            case 7518:
                return 7532;
            case 7415:
                return 7419;
            case 7414:
                return 7418;
            case 6978:
                return 7417;
            case 6716:
                return 7420;
            case 2077:
                return 2098;
            case 2076:
                return 2101;
            case 2075:
                return 2100;
            case 2074:
                return 2099;
            case 1748:
                return 2102;
            case 1728:
                return 1735;
        }
        return -1;
    }

    public static int getPetsByIdParcho(int id) {
        switch (id) {
            case 10806:
                return 10802;
            case 10135:
                return 10107;
            case 10134:
                return 10106;
            case 9810:
                return 9795;
            case 9685:
                return 9624;
            case 9684:
                return 9623;
            case 9683:
                return 9620;
            case 9682:
                return 9619;
            case 9675:
                return 9617;
            case 9598:
                return 9594;
            case 8707:
                return 8693;
            case 8684:
                return 8677;
            case 8564:
                return 8561;
            case 8544:
                return 8211;
            case 8179:
                return 8155;
            case 8178:
                return 8154;
            case 8175:
                return 8153;
            case 8176:
                return 8151;
            case 8180:
                return 8000;
            case 8526:
                return 7911;
            case 7896:
                return 7892;
            case 7895:
                return 7891;
            case 8708:
                return 7714;
            case 9681:
                return 7713;
            case 9680:
                return 7712;
            case 9679:
                return 7711;
            case 9678:
                return 7710;
            case 9677:
                return 7709;
            case 9676:
                return 7708;
            case 9674:
                return 7707;
            case 8685:
                return 7706;
            case 8889:
                return 7705;
            case 8888:
                return 7704;
            case 8421:
                return 7703;
            case 8887:
                return 7524;
            case 7535:
                return 7522;
            case 7533:
                return 7520;
            case 7534:
                return 7519;
            case 7532:
                return 7518;
            case 7419:
                return 7415;
            case 7418:
                return 7414;
            case 7417:
                return 6978;
            case 7420:
                return 6716;
            case 2098:
                return 2077;
            case 2101:
                return 2076;
            case 2100:
                return 2075;
            case 2099:
                return 2074;
            case 2102:
                return 1748;
            case 1735:
                return 1728;
        }
        return -1;
    }

    public static int getDoplonDopeul(int IDmob) {
        switch (IDmob) {
            case 168:
                return 10302;
            case 165:
                return 10303;
            case 166:
                return 10304;
            case 162:
                return 10305;
            case 160:
                return 10306;
            case 167:
                return 10307;
            case 161:
                return 10308;
            case 2691:
                return 10309;
            case 455:
                return 10310;
            case 169:
                return 10311;
            case 163:
                return 10312;
            case 164:
                return 10313;
        }
        return -1;
    }

    public static int getIDdoplonByMapID(int IDmap) {
        switch (IDmap) {
            case 6926: //Sram
                return 10312;
            case 1470: //Enutrof
                return 10305;
            case 1461: //Ecaflip (map de dessous, puisque l'autre n'est pas dans l'emu)
                return 10303;
            case 6949: //Sacrieur
                return 10310;
            case 1556: //Cra (map en bas dans la maison, celle dans haut n'est pas dans l'emu)
                return 10302;
            case 1549: //Iop
                return 10307;
            case 1469: //Xel
                return 10313;
            case 487: //Eniripsa (dehors, puisque l'int�rieur n'est pas pr�sent dans l'emu)
                return 10304;
            case 490: //Osamodas (idem qu'eniripsa)
                return 10308;
            case 177: //Feca (idem ...)
                return 10306;
            case 1466: //Sadida
                return 10311;
            case 8207: //Panda (idem que nini ...)
                return 10309;
        }
        return -1;
    }

    public static int getArmeSoin(int idArme) {
        switch (idArme) {
            case 7172:
                return 100;
            case 7156:
                return 80;
            case 1355:
                return 42;
            case 7182:
                return 100;
            case 7040:
                return 10;
            case 6539:
                return 80;
            case 6519:
                return 23;
            case 8118:
                return 30;
            default:
                return -1;
        }
    }

    public static int getSectionByDopeuls(int id) {
        switch (id) {
            case 160:
                return 1;
            case 161:
                return 2;
            case 162:
                return 3;
            case 163:
                return 4;
            case 164:
                return 5;
            case 165:
                return 6;
            case 166:
                return 7;
            case 167:
                return 8;
            case 168:
                return 9;
            case 169:
                return 10;
            case 455:
                return 11;
            case 2691:
                return 12;
        }
        return -1;
    }

    public static int getCertificatByDopeuls(int id) {
        switch (id) {
            case 160:
                return 10293;
            case 161:
                return 10295;
            case 162:
                return 10292;
            case 163:
                return 10299;
            case 164:
                return 10300;
            case 165:
                return 10290;
            case 166:
                return 10291;
            case 167:
                return 10294;
            case 168:
                return 10289;
            case 169:
                return 10298;
            case 455:
                return 10297;
            case 2691:
                return 10296;
        }
        return -1;
    }

    public static boolean isCertificatDopeuls(int id) {
        switch (id) {
            case 10293:
            case 10295:
            case 10292:
            case 10299:
            case 10300:
            case 10290:
            case 10291:
            case 10294:
            case 10289:
            case 10298:
            case 10297:
            case 10296:
                return true;
        }
        return false;
    }

    public static int getItemIdByMascotteId(int id) {
        switch (id) {
            case 10118:
                return 1498;//Croc blanc
            case 10078:
                return 70;//Eni Hoube
            case 10077:
                return -1;//Terra Cogita
            case 10009:
                return 90;//Xephir�s
            case 9993:
                return 71;//Sabine
            case 9096:
                return 30;//Tacticien
            case 9061:
                return 40;//Exoram
            case 8563:
                return 1076;//Titi gobelait
            case 7425:
                return 1588;//Petite Larve Dor�e
            case 7354:
                return 1264;//Zato�shwan
            case 7353:
                return 1076;//Marzwell Le Gobelin
            case 7352:
                return 1153;//Musha l'Oni
            case 7351:
                return 1248;//Rok Gnorok
            case 7350:
                return 1228;//Aermyne Braco Scalptaras
            case 7062:
                return 9001;//Poochan
            case 6876:
                return 1245;//Ogivol Scarlarcin
            case 6875:
                return 1249;//Fouduglen
            case 6874:
                return 70;//Brumen Tinctorias
            case 6873:
                return 1243;//Qil Bil
            case 6872:
                return 50;//Nervoes Brakdoun
            case 6871:
                return 1247;//Frakacia Leukocytine
            case 6870:
                return 1246;//Padgref Demo�l
            case 6869:
                return 9043;//Pleur Nycheuz
            case 6832:
                return -1;//Livreur de Bi�re
            case 6768:
                return 9001;//Soki
            case 2272:
                return 1577;//Larve Dor�e
            case 2169:
                return 1205;//Raaga
            case 2152:
                return 1001;//Colonel Lyeno
            case 2134:
                return 1205;//Trof Hapyus
            case 2132:
                return 9004;//Hou� Dapyus
            case 2130:
                return 1001;//Colonel Lyeno
            case 2082:
                return 1208;//Marcassin
        }
        return -1;
    }

    public static boolean isIncarnationWeapon(int id) {
        switch (id) {
            case 9544:
            case 9545:
            case 9546:
            case 9547:
            case 9548:
            case 10133:
            case 10127:
            case 10126:
            case 10125:
                return true;
        }
        return false;
    }

    public static boolean isTourmenteurWeapon(int id) {
        switch (id) {
            case 9544:
            case 9545:
            case 9546:
            case 9547:
            case 9548:
                return true;
        }
        return false;
    }

    public static boolean isBanditsWeapon(int id) {
        switch (id) {
            case 10133:
            case 10127:
            case 10126:
            case 10125:
                return true;
        }
        return false;
    }

    public static int getSpecialSpellByClasse(int classe) {
        switch (classe) {
            case Constant.CLASS_FECA:
                return 422;
            case Constant.CLASS_OSAMODAS:
                return 420;
            case Constant.CLASS_ENUTROF:
                return 425;
            case Constant.CLASS_SRAM:
                return 416;
            case Constant.CLASS_XELOR:
                return 424;
            case Constant.CLASS_ECAFLIP:
                return 412;
            case Constant.CLASS_ENIRIPSA:
                return 427;
            case Constant.CLASS_IOP:
                return 410;
            case Constant.CLASS_CRA:
                return 418;
            case Constant.CLASS_SADIDA:
                return 426;
            case Constant.CLASS_SACRIEUR:
                return 421;
            case Constant.CLASS_PANDAWA:
                return 423;
        }
        return 0;
    }

    public static boolean isFlacGelee(int id) {
        switch (id) {
            case 2430:
            case 2431:
            case 2432:
            case 2433:
                return true;
        }
        return false;
    }

    public static boolean isDoplon(int id) {
        switch (id) {
            case 10302:
            case 10303:
            case 10304:
            case 10305:
            case 10306:
            case 10307:
            case 10308:
            case 10309:
            case 10310:
            case 10311:
            case 10312:
            case 10313:
                return true;
        }
        return false;
    }

    public static boolean isInMorphDonjon(int id) {
        switch (id) {
            case 8716:
            case 8718:
            case 8719:
            case 9121:
            case 9122:
            case 9123:
            case 8979:
            case 8980:
            case 8981:
            case 8982:
            case 8983:
            case 8984:
            case 9716:
                return true;
        }
        return false;
    }

    public static int[] getOppositeStats(int statsId) {
        if (statsId == 217)
            return new int[]{210, 211, 213, 214};
        else if (statsId == 216)
            return new int[]{210, 212, 213, 214};
        else if (statsId == 218)
            return new int[]{210, 211, 212, 214};
        else if (statsId == 219)
            return new int[]{210, 211, 212, 214};
        else if (statsId == 215)
            return new int[]{211, 212, 213, 214};
        return null;
    }

    public static int getNearestCellIdUnused(Player player) {
        final GameMap map = player.getCurMap();
        final int width = map.getW();
        final int cell = player.getCurCell().getId();
        final int[] cells = new int[] {cell - width, cell - width + 1, cell + width - 1, cell + width};
        int cellPosition = -1;

        for(int available : cells) {
            GameCase c = map.getCase(available);
            if (c != null && c.getDroppedItem(false) == null && c.getPlayers().isEmpty() && c.isWalkable(false) && c.getObject() == null) {
                return available;
            }
        }
        return -1;
    }


    public static short getTrapsAnimation(int spell) {
        switch(spell) {
            case 73: // Repulsif
                return 409;
            case 79: // Masse
                return 410;
            case 77: // Silence
                return 411;
            default:
                return 407;
        }
    }

    public static boolean MimibioteItem(int type){
        boolean ok = false;
        switch(type){
            case ITEM_TYPE_CAPE:
            case ITEM_TYPE_COIFFE:
            case ITEM_TYPE_BOUCLIER:
            case ITEM_TYPE_FAMILIER:
                ok = true;
                break;
        }
        return ok;
    }

    public static final int DISPLAY_WIDTH = 742;
    public static final int DISPLAY_HEIGHT = 432;
    public static final int CELL_WIDTH = 53;
    public static final int CELL_HEIGHT = 27;
    public static final double CELL_HALF_WIDTH = 26.5;
    public static final double CELL_HALF_HEIGHT = 13.5;
    public static final int LEVEL_HEIGHT = 20;
    public static final int HALF_LEVEL_HEIGHT = 10;
    public static final int DEFAULT_MAP_WIDTH = 15;
    public static final int DEFAULT_MAP_HEIGHT = 17;
    public static final int MAX_DEPTH_IN_MAP = 100000;
    public static final double[][][] CELL_COORD = {
            {},
            {{-26.5, 0}, {0, -13.5}, {26.5, 0}, {0, 13.5}},
            {{-26.5, -20}, {0, -13.5}, {26.5, 0}, {0, 13.5}},
            {{-26.5, 0}, {0, -33.5}, {26.5, 0}, {0, 13.5}},
            {{-26.5, -20}, {0, -33.5}, {26.5, 0}, {0, 13.5}},
            {{-26.5, 0}, {0, -13.5}, {26.5, -20}, {0, 13.5}},
            {{-26.5, -20}, {0, -13.5}, {26.5, -20}, {0, 13.5}},
            {{-26.5, 0}, {0, -33.5}, {26.5, -20}, {0, 13.5}},
            {{-26.5, -20}, {0, -33.5}, {26.5, -20}, {0, 13.5}},
            {{-26.5, 0}, {0, -13.5}, {26.5, 0}, {0, -6.5}},
            {{-26.5, -20}, {0, -13.5}, {26.5, 0}, {0, -6.5}},
            {{-26.5, 0}, {0, -33.5}, {26.5, 0}, {0, -6.5}},
            {{-26.5, -20}, {0, -33.5}, {26.5, 0}, {0, -6.5}},
            {{-26.5, 0}, {0, -13.5}, {26.5, -20}, {0, -6.5}},
            {{-26.5, -20}, {0, -13.5}, {26.5, -20}, {0, -6.5}},
            {{-26.5, 0}, {0, -33.5}, {26.5, -20}, {0, -6.5}}
    };


    public static ArrayList<Integer> getParcheminMetierID() {
        ArrayList<Integer> gemmespi = new ArrayList<>();

        gemmespi.add(695);


        for (int i = 713; i <= 717; i++) {
                gemmespi.add(i);
        }
        gemmespi.add(878);
        gemmespi.add(879);
        for (int i = 10382; i <= 10407; i++) {
            gemmespi.add(i);
        }
        return gemmespi;
    }


    public static ArrayList<Integer> getGemmesSpritiuelsID() {
        ArrayList<Integer> gemmespi = new ArrayList<>();

        for (int i = 10227; i <= 10270; i++) {
            if (i != 10232 && i != 10243) {
                gemmespi.add(i);
            }
        }

        gemmespi.add(10278);
        gemmespi.add(10606);
        gemmespi.add(11567);
        gemmespi.add(11568);

        return gemmespi;
    }

    public static Integer getRandomGemmesSpritiuels() {
        Random rand = new Random();
        int randomIndex = rand.nextInt(ISSPIRITGEM.size());
        int randomNum = ISSPIRITGEM.get(randomIndex);
        return randomNum;
    }

    public static boolean isInGladiatorDonjon(int id) {
        switch (id) {
            case 15000:
            case 15008:
            case 15016:
            case 15024:
            case 15032:
            case 15040:
            case 15048:
            case 15056:
            case 15064:
            case 15072:
                return true;
        }
        return false;
    }

    public static final int[] TONIQUE1 = {16002,16003,16004,16005,16006,16007,16008,16009,16010,16011,16012};
    public static final int[] TONIQUE2 = {16013,16014,16015,16016,16017,16018,16019,16020,16021,16022,16023};



    public static ArrayList<Integer> getToniques3byclasse(int classeid) {
        ArrayList<Integer> tonique3 = new ArrayList();
        int j = 16007+(classeid*20);
        for(int i = j; i <= (j+19); i++) {
            tonique3.add(i);
        }

        return tonique3;
       /* switch (classeid){
            case Constant.CLASS_FECA:
                for(int i = 16027; i <=16046; i++){
                    tonique3.add(i);
                }
                break;
            case Constant.CLASS_OSAMODAS:
                for(int i = 16047; i <=16046; i++){
                    tonique3.add(i);
                }
                tonique3 = new int[]{16047,16048,16049,16050,16051,16052,16053,16054,16055,16056,16057,16058,16059,16060,16061,16062,16063,16064,16065,16066};
                break;
            case Constant.CLASS_ENUTROF:
                tonique3 = new int[]{16067,16068,16069,16070,16071,16072,16073,16074,16075,16076,16077,16078,16079,16080,16081,16082,16083,16084,16085,16086};
                break;
            case Constant.CLASS_SRAM:
                tonique3 = new int[]{16087,16088,16089,16090,16091,16092,16093,16094,16095,16096,16097,16098,16099,16100,16101,16102,16103,16104,16105,16106};
                break;
            case Constant.CLASS_XELOR:
                tonique3 = new int[]{16107,16108,16109,16110,16111,16112,16113,16114,16115,16116,16117,16118,16119,16120,16121,16122,16123,16124,16125,16126};
                break;
            case Constant.CLASS_ECAFLIP:
                tonique3 = new int[]{16127,16128,16129,16130,16131,16132,16133,16134,16135,16136,16137,16138,16139,16140,16141,16142,16143,16144,16145,16146};
                break;
            case Constant.CLASS_ENIRIPSA:
                tonique3 = new int[]{16147,16148,16149,16150,16151,16152,16153,16154,16155,16156,16157,16158,16159,16160,16161,16162,16163,16164,16165,16166};
                break;
            case Constant.CLASS_IOP:
                tonique3 = new int[]{16167,16168,16169,16170,16171,16172,16173,16174,16175,16176,16177,16178,16179,16180,16181,16182,16183,16184,16185,16186};
                break;
            case Constant.CLASS_CRA:
                tonique3 = new int[]{16187,16188,16189,16190,16191,16192,16193,16194,16195,16196,16197,16198,16199,16200,16201,16202,16203,16204,16205,16206};
                break;
            case Constant.CLASS_SADIDA:
                tonique3 = new int[]{16207,16208,16209,16210,16211,16212,16213,16214,16215,16216,16217,16218,16219,16220,16221,16222,16223,16224,16225,16226};
                break;
            case Constant.CLASS_SACRIEUR:
                tonique3 = new int[]{16227,16228,16229,16230,16231,16232,16233,16234,16235,16236,16237,16238,16239,16240,16241,16242,16243,16244,16245,16246};
                break;
            case Constant.CLASS_PANDAWA:
                tonique3 = new int[]{16247,16248,16249,16250,16251,16252,16253,16254,16255,16256,16257,16258,16259,16260,16261,16262,16263,16264,16265,16266};
                break;
        }


        return tonique3;*/
    }

    public static String getStatStringbyPalier(int palier) {
        switch (palier) {
            case 6:
                return "844#0###0d0+0,6f#1#0#0#0d0+1,7d#fa###0d0+250,7c#32###0d0+50,76#32###0d0+50,7e#32###0d0+50,7b#32###0d0+50,77#32###0d0+50,70#3###0d0+3,8a#1e###0d0+30,b2#5###0d0+5,73#3###0d0+3,d2#1###0d0+1,d5#1###0d0+1,d3#1###0d0+1,d4#1###0d0+1,d6#1###0d0+1";
            case 8:
                return "844#0###0d0+0,6f#1#0#0#0d0+1,80#1#0#0#0d0+1,7d#fa###0d0+250,7c#32###0d0+50,76#32###0d0+50,7e#32###0d0+50,7b#32###0d0+50,77#32###0d0+50,70#3###0d0+3,8a#1e###0d0+30,b2#5###0d0+5,73#3###0d0+3,d2#1###0d0+1,d5#1###0d0+1,d3#1###0d0+1,d4#1###0d0+1,d6#1###0d0+1";
            case 10:
                return "844#0###0d0+0,7d8#64#0#0#0d0+100,6f#1#0#0#0d0+1,80#1#0#0#0d0+1,7d#fa###0d0+250,7c#32###0d0+50,76#32###0d0+50,7e#32###0d0+50,7b#32###0d0+50,77#32###0d0+50,70#3###0d0+3,8a#1e###0d0+30,b2#5###0d0+5,73#3###0d0+3,d2#1###0d0+1,d5#1###0d0+1,d3#1###0d0+1,d4#1###0d0+1,d6#1###0d0+1";
            default:
                return "844#0###0d0+0,7d#fa###0d0+250,7c#32###0d0+50,76#32###0d0+50,7e#32###0d0+50,7b#32###0d0+50,77#32###0d0+50,70#3###0d0+3,8a#1e###0d0+30,b2#5###0d0+5,73#3###0d0+3,d2#1###0d0+1,d5#1###0d0+1,d3#1###0d0+1,d4#1###0d0+1,d6#1###0d0+1";
        }
    }

    public static int getClasseByMorphWeapon(int MorphWeapon) {
        int Classe = MorphWeapon-12781;
        return Classe;
    }

    public static int getPalierByNewMap(int Mapid) {
        switch (Mapid) {
            case 12277:
            case 15000:
                return 1;
            case 15008:
                return 2;
            case 15016:
                return 3;
            case 15024:
                return 4;
            case 15032:
                return 5;
            case 15040:
                return 6;
            case 15048:
                return 7;
            case 15056:
                return 8;
            case 15064:
                return 9;
            case 15072:
                return 10;
        }
        return 0;
    }

    public static boolean isGladiatroolWeapon(int id) {
        if(id >= 12782 && id <= 12793)
            return true;
        return false;
    }

    public static boolean isToniquePos(int pos) {
        switch(pos){
            case ITEM_POS_TONIQUE_EQUILIBRAGE:
            case ITEM_POS_TONIQUE1:
            case ITEM_POS_TONIQUE2:
            case ITEM_POS_TONIQUE3:
            case ITEM_POS_TONIQUE4:
            case ITEM_POS_TONIQUE5:
            case ITEM_POS_TONIQUE6:
            case ITEM_POS_TONIQUE7:
            case ITEM_POS_TONIQUE8:
            case ITEM_POS_TONIQUE9:
            return true;
        }
      return false;
    }

    public static boolean isGladiatroolMorph(int id) {
        if(id >= 101 && id <= 112)
            return true;
        return false;
    }

}