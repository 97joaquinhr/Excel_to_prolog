:-include('info/data.pl').

nombre_apellidoP(Nomina,Nombre,Apellido):-
    nomina_Nombrep(Nomina,Nombre),
    nomina_Apaternop(Nomina,Apellido).
