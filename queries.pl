:-include('info/data.pl').

%helper append function
appendM([],List1,List1).
appendM([Head|Tail],List2,[Head|Result]):-
    append(Tail, List2, Result).

nombre_apellidoP(Nomina,Nombre,Apellido):-
    nomina_Nombrep(Nomina,Nombre),
    nomina_Apaternop(Nomina,Apellido).

cursosH([],X,X):-!.

cursosH([H|T],X,Res):-
    crn_NombreMateria(H,Curso),
    appendM(X,[Curso],NewX),
    cursosH(T,NewX,Res).

cursos(Nomina,X):-
    findall(CRN,crn_Nómina(CRN,Nomina),Aux),
    cursosH(Aux,[],X).
    /*findall(Curso,crn_NombreMateria(CRN,Curso),X).*/

cursos_en_salon(Salon,X):-
    findall(CRN,crn_Sal1(CRN,Salon),Aux),
    cursosH(Aux,[],X).
