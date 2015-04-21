/* 
 * This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/* 
 * @Author Lerk
 * 
 * Shawn, Victoria Road: Excavation Site<Camp> (101030104)
 * 
 * Guild Quest Info
 */

var status;
var selectedOption;

function start() {
        selectedOption = -1;
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
                if (mode == 1 && status == 3) {
                        status = 0;
                }
		if (status == 0) {
                        var prompt = "\r\n#b#L0#Oque e Sharenian?#l\r\n#b#L1##t4001024#, o que e isso?#l\r\n#b#L2#Guild Quest?#l\r\n#b#L3#Nao, eu estou bem agora.#l";
                        if (selectedOption == -1) {
                                prompt = "Nos, da Guild Quest temos tentado decifrar a \"Tabua de Esmeralda\", uma antiga reliquia preciosa, que por um longo tempo ficou adormecida por aqui. Como resultado, descobrimos que Sharenian, o pais misterioso do passado, dormia aqui.. Nos tambem descobrimos que as pistas de #t4001024#, um lendario, mitico das joias, que pode ser aqui que os restos de Sharenian estao. e por isso que a Guild Quest abriu a quest para encontrar #t4001024#." + prompt;
                        } else {
                                prompt = "Do you have any other questions?" + prompt;
                        }
                        cm.sendSimple(prompt);
                } 
                else if (status == 1) {
                        selectedOption = selection;
                        if (selectedOption == 0) {
                                cm.sendNext("Sharenian era uma civilizacao letrada do passado que tinha o controle sobre todas as areas da Ilha Victoria. O Templo do Golem , o Santuario na parte profunda da Dungeon, e outras construcoes arquitetonicas antigas , onde ninguem sabe quem o construiu sao realmente feitas durante os tempos Sharenian.");
                        }
                        else if (selectedOption == 1) {
                                cm.sendNext("#t4001024# e uma joia lendaria que traz a juventude eterna para aquele que o possui. Ironicamente, parece que todo mundo que teve #t4001024# acabaram oprimidos, o que deve explicar a queda de Sharenian ");
                                status = -1;
                        }
                        else if (selectedOption == 2) {
                                cm.sendNext("Mandei grupos de exploradores para Sharenian antes, mas nenhum deles jamais voltou, o que nos levou a iniciar o Guild quest. Nos estivemos esperando por guild's que sao fortes o suficiente para assumir desafios dificeis.");
                        }
                        else if (selectedOption == 3) {
                                cm.sendOk("Realmente? Se voce tem mais alguma coisa para perguntar, por favor, sinta-se livre para falar comigo.");
                                cm.dispose();
                        }
                        else {
                                cm.dispose();
                        }
                }
                else if (status == 2) { //should only be available for options 0 and 2
                        if (selectedOption == 0) {
                                cm.sendNextPrev("O ultimo rei da Sharenian era um cavalheiro chamado Sharen III , e, aparentemente, ele era um rei muito sabio e compassivo. Mas um dia, todo o reino entrou em colapso, e nao havia explicação feita para ele.");
                        }
                        else if (selectedOption == 2) {
                                cm.sendNextPrev("O objetivo final desta Guild Quest e explorar Sharenian e encontrar #t4001024#. Esta nao e uma tarefa em que o poder resolve tudo. Trabalho em equipe e mais importante aqui!");
                        }
                        else {
                                cm.dispose();
                        }
                }
        }
}