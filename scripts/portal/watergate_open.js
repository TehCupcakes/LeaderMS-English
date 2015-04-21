importPackage(Packages.server.maps);

/*
Stage 3: Exit Door - Guild Quest
@Author Lerk
*/

function enter(pi) {
    if (pi.getPlayer().getMap().getReactorByName("watergate").getState() == 1) {
        pi.warp(990000600);
        return true;
    }
    else
    pi.playerMessage("This way forward is not open yet.");
    return false;
}
