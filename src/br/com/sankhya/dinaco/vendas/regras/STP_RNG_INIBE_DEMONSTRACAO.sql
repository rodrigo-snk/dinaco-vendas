create or replace PROCEDURE STP_RNG_INIBE_DEMONSTRACAO (P_NUNOTA INT, P_SEQUENCIA INT, P_SUCESSO OUT VARCHAR, P_MENSAGEM OUT VARCHAR2, P_CODUSULIB OUT NUMBER)
AS
BEGIN

DECLARE
   V_USOPROD VARCHAR2(1);
   V_CODTIPOPER INT;
   V_DESCROPER VARCHAR2(40);

BEGIN
     SELECT USOPROD, CAB.CODTIPOPER, TOP.DESCROPER INTO V_USOPROD, V_CODTIPOPER, V_DESCROPER
     FROM TGFITE ITE
     INNER JOIN TGFCAB CAB ON ITE.NUNOTA = CAB.NUNOTA
     INNER JOIN TGFTOP TOP ON CAB.CODTIPOPER = TOP.CODTIPOPER AND CAB.DHTIPOPER = TOP.DHALTER
     WHERE ITE.NUNOTA = P_NUNOTA
     AND ITE.SEQUENCIA = P_SEQUENCIA;
     --USOPROD = '4' (Demonstração)
IF V_USOPROD = '4' THEN
    P_SUCESSO := 'N';
    P_MENSAGEM := 'Tipo Operação ' || V_CODTIPOPER || ' - ' || V_DESCROPER || ' não permite produtos usados como demonstração/amostra. Use um tipo de operação que permita amostras.';
 ELSE
    P_SUCESSO := 'S';
END IF;

END;

END;