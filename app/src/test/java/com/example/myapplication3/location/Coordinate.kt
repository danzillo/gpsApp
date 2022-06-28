package com.example.myapplication3.location

class Coordinate(
    var longitude: Double,
    var latitude: Double
)

val distanceMarks = mutableListOf(
    Coordinate(84.9398207016564, 56.4471399261583), // 1 - ЛЭП (0+804; 1+000)
    Coordinate(84.9547917282365, 56.4429847327726), // 2 - дерево (1+1068; 2+000)
    Coordinate(84.9579233722567, 56.4330278731898)  // 3 - начало дач (2+1133; 3+000)
)

val axis = mutableListOf(
    Coordinate(84.92880858269907662, 56.45087313041989319),
    Coordinate(84.92908405256977744, 56.45069672618978984),
    Coordinate(84.92932016960176611, 56.45053240370440761),
    Coordinate(84.92959126693479277, 56.45037049703108778),
    Coordinate(84.92982738396679565, 56.45024967070589383),
    Coordinate(84.93024714757925153, 56.45009742898908911),
    Coordinate(84.93084618523450047, 56.44987269010177755),
    Coordinate(84.93207049577084433, 56.44943045808718551),
    Coordinate(84.93309366957618067, 56.44905105274846591),
    Coordinate(84.93385449112373919, 56.44876347547726425),
    Coordinate(84.93454972460686747, 56.44849039588571316),
    Coordinate(84.93477272402597578, 56.44840339666556872),
    Coordinate(84.93516187987501098, 56.44827289746185528),
    Coordinate(84.9358090154442209, 56.44808439782084264),
    Coordinate(84.93610197546537677, 56.44799981434461245),
    Coordinate(84.93663105251859236, 56.4478572303442192),
    Coordinate(84.93710765912022964, 56.44771706250031684),
    Coordinate(84.93768920662496669, 56.44755031042633675),
    Coordinate(84.93806961739875305, 56.44745847563835639),
    Coordinate(84.93848938101118051, 56.44736905734202992),
    Coordinate(84.93882606640869426, 56.44732072304021386),
    Coordinate(84.93985798528929365, 56.44719022011817344),
    Coordinate(84.94071063012707157, 56.44707180041216787),
    Coordinate(84.94182999976024462, 56.44691954596218153),
    Coordinate(84.94329042732859136, 56.44671653907993658),
    Coordinate(84.94456720831644247, 56.4465401155514428),
    Coordinate(84.94559038212177882, 56.44640719317388289),
    Coordinate(84.94623751769098874, 56.44632018918407113),
    Coordinate(84.94667914399158803, 56.4462646031973847),
    Coordinate(84.9472125935823783, 56.44617034851243176),
    Coordinate(84.94768045510878096, 56.44607367679782328),
    Coordinate(84.94829698291457021, 56.44592625195955549),
    Coordinate(84.94897035370952665, 56.44578366017842797),
    Coordinate(84.94974866540761127, 56.44563381739120445),
    Coordinate(84.95010284095560849, 56.44553956114169324),
    Coordinate(84.95038705590154393, 56.44544047098628425),
    Coordinate(84.95075434906242151, 56.44528579315407057),
    Coordinate(84.95115224998671977, 56.44509486183667946),
    Coordinate(84.95186497362035993, 56.44475891706338189),
    Coordinate(84.95248587396376649, 56.44443505382297843),
    Coordinate(84.95304993131797744, 56.4441426077793551),
    Coordinate(84.95376702748924913, 56.44376556658387756),
    Coordinate(84.95405124243518458, 56.44358429544683275),
    Coordinate(84.95429610454246472, 56.44339577254714158),
    Coordinate(84.95483392667091493, 56.44288578924729194),
    Coordinate(84.9552362001328305, 56.44250390153720076),
    Coordinate(84.95548106224008222, 56.44224044512068161),
    Coordinate(84.95563847359476028, 56.44203499618905795),
    Coordinate(84.9557871398741753, 56.44178845600514904),
    Coordinate(84.95587459062676317, 56.44159750711666845),
    Coordinate(84.9560101392932836, 56.4411841840850812),
    Coordinate(84.95618941333610508, 56.44061374257722719),
    Coordinate(84.95634682469078314, 56.43999011452290659),
    Coordinate(84.95640804021759607, 56.43926495275631083),
    Coordinate(84.95643864798100253, 56.43875733128796668),
    Coordinate(84.95646488320676326, 56.43849143162288584),
    Coordinate(84.95657856918512607, 56.43811916896690661),
    Coordinate(84.95679719606661706, 56.43738188476466178),
    Coordinate(84.95691525458263982, 56.4370434544407118),
    Coordinate(84.9570420581738972, 56.43647295078842063),
    Coordinate(84.95716448922750885, 56.43607891148725741),
    Coordinate(84.9573918611842771, 56.43546729638403292),
    Coordinate(84.95764109582914614, 56.43480490371076996),
    Coordinate(84.95775478180755158, 56.4344350224883442),
    Coordinate(84.95788158539880897, 56.43363722720796005),
    Coordinate(84.95791219316221543, 56.43335920370693515),
    Coordinate(84.95799089883955446, 56.43303282569843304),
    Coordinate(84.95813082004372063, 56.4325227033474448),
    Coordinate(84.95814393765658679, 56.43232687256998759),
    Coordinate(84.95810458481793148, 56.43210202858164592),
    Coordinate(84.95804336929111855, 56.43197147339758146),
    Coordinate(84.95789907554933507, 56.43176838666465756),
    Coordinate(84.95784223256013945, 56.43167167831533959),
    Coordinate(84.95775040926993427, 56.43145891908096701),
    Coordinate(84.95766733105497792, 56.43124857639481462),
    Coordinate(84.95760611552816499, 56.43105999230760261),
    Coordinate(84.95759737045288773, 56.43095602888522677),
    Coordinate(84.95760174299050504, 56.43087140728504636),
    Coordinate(84.95773291911942238, 56.43057885687348119),
    Coordinate(84.95805648690401313, 56.43006870159732102),
    Coordinate(84.95837130961335504, 56.42961414989785851),
    Coordinate(84.95878232815053366, 56.42901210231435272),
    Coordinate(84.95900970010727349, 56.42869294069320318),
    Coordinate(84.959451326407887, 56.42803768381803309),
    Coordinate(84.95967869836461261, 56.42771851401826666)
)