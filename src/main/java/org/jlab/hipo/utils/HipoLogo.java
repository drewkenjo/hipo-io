/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.hipo.utils;

/**
 *
 * @author gavalian
 */
public class HipoLogo {
    
    /* MADE BY : http://patorjk.com/software/taag/ */
    
    public static String[] hipoWORD_ASCII_1 = new String[]{
        "888    888 8888888 8888888b.   .d88888b.        .d8888b.       .d8888b.",
        "888    888   888   888   Y88b d88P\" \"Y88b      d88P  Y88b     d88P  Y88b",
        "888    888   888   888    888 888     888             888     888    888",
        "8888888888   888   888   d88P 888     888           .d88P     888    888",
        "888    888   888   8888888P\"  888     888       .od888P\"      888    888",
        "888    888   888   888        888     888      d88P\"          888    888",
        "888    888   888   888        Y88b. .d88P      888\"       d8b Y88b  d88P\"",
        "888    888 8888888 888         \"Y88888P\"       888888888  Y8P  \"Y8888P\""
    };
    
    public static String[] hipoASCII_1 = new String[]{
        
        "     ..ed$$$$$$$$$$$$$be      *F..",
        "^   z$$$$$$$$$$$$$$$$$$$$$$$$$$$$",
        "  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$.",
        " $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*e.",
        "4$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$be$$$$$$c",
        "4$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$L",
        "^$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$",
        " ^*$$$$$$$$$$$$$$$$$$$$$$$$$$$F^*$$$$$$$$$%",
        "   ^\"$$$$$$$$$$$$$$$$$$$$$$$$$    \"**$P*\"",  
        "      4$$$F\"3$$$$       4$$$$",
        "      d$$$$ 4$$$$       4$$$$"                                                     
    };
    
    
    public static String[] hipoASCII_2 = new String[]{
        "                   .^.,*.",
        "                  (   )  )",
        "                 .~       \"-._   _.-'-*'-*'-*'-*'-'-.--._",
        "               /'             `\"'                        `.",
        "             _/'                                           `.",
        "        __,\"\"                                                ).--.",
        "     .-'       `._.'                                          .--.\\",
        "    '                                                         )   \\`:",
        "   ;                                                          ;    \"",
        "  :                                                           )",
        "  | 8                                                        ;",
        "   =                  )                                     .",
        "    \\                .                                    .'",
        "      `.            ~  \\                                .-'",
        "        `-._ _ _ . '    `.          ._        _        |",
        "                          |        /  `\"-*--*' |       |            _   _ ___ ____   ___    ____    ___ ",
        "                          |        |           |       :           | | | |_ _|  _ \\ / _ \\  |___ \\  / _ \\",
        "~~~~~~~---   ~-~-~-~   -~-~-~-~-~-~~~~~~  ~~~~  ~-~-~-~-~-~-~-     | |_| || || |_) | | | |   __) || | | |",
        "------~~~~~~~~~----------~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~     |  _  || ||  __/| |_| |  / __/ | |_| |",
        "~~~~~~~~~   ~~~~~~~~~       ~~~~~~~   ~~~~~~~~~  ~~~~~~~~~~~~~~~   |_| |_|___|_|    \\___/  |_____(_)___/"
    };
            
    public static void showLogo(){
        System.out.println("\n\n");
        for(int i = 0; i < HipoLogo.hipoASCII_2.length; i++){
            System.err.println("    " + HipoLogo.hipoASCII_2[i]);
        }
        /*
        for(int i = 0; i < HipoLogo.hipoASCII_1.length; i++){
            System.err.println("    " + HipoLogo.hipoASCII_1[i]);
        }*/
        
        
        System.out.println("\n");
    }
    
    
    public static void showVersion(int style){
        if(style==0){
            for(int i = 0; i < HipoLogo.hipoWORD_ASCII_1.length;i++){
                System.out.println("    " + HipoLogo.hipoWORD_ASCII_1[i]);
            }
        }
    }
}
