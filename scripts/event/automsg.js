/*
 * LeaderMS 2012 
 */
var setupTask;

function init() {
    scheduleNew();
}

function scheduleNew() {
    var cal = java.util.Calendar.getInstance();
    cal.set(java.util.Calendar.HOUR, 1);
    cal.set(java.util.Calendar.MINUTE, 0);
    cal.set(java.util.Calendar.SECOND, 0);
    var nextTime = cal.getTimeInMillis();
    while (nextTime <= java.lang.System.currentTimeMillis())
        nextTime += 300 * 1000;
    setupTask = em.scheduleAtTimestamp("start", nextTime);
}

function cancelSchedule() {
    setupTask.cancel(true);
}

function start() {
    scheduleNew();
    var Message = new Array("Welcome to "+em.getServerName()+"!" ,"Please refrain from using profane language in the game.", "Verbal abuse or any other form of abuse will not be tolerated in this game. Users who break the rules may be blocked from the game.","Report any errors/bugs using the @bug command.","Use @command to see a list of available commands!");
    em.getChannelServer().yellowWorldMessage("["+em.getServerName()+" Tip] " + Message[Math.floor(Math.random() * Message.length)]);
}