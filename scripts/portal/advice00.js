function enter(pi) {
	if (pi.getPlayer().getClient().isGuest()) {
		pi.showInstruction("Welcome to the server!\r\nCurrently logged in as a guest? Create a new account if you like the server.", 350, 5);
	} else {
		pi.showInstruction("You can move by using the arrow keys.", 250, 5);
	}
    return true;
}