create or replace PROCEDURE STP_RNG_OPORTUNIDADE (P_NUNOTA INT, P_SUCESSO OUT VARCHAR, P_MENSAGEM OUT VARCHAR2, P_CODUSULIB OUT NUMBER)
AS
BEGIN

DECLARE
   V_STATUS VARCHAR2(3);

    BEGIN
         SELECT OP.STATUS INTO V_STATUS
         FROM AD_NEGVENDA OP
         JOIN AD_NEGVENDA1A AM ON OP.NUNEGOCIACAO = AM.NUNEGOCIACAO
         WHERE AM.NUNOTA = P_NUNOTA;

        CASE V_STATUS
            WHEN 'CAN' THEN
                P_SUCESSO := 'N';
                P_MENSAGEM := 'Oportunidade cancelada. Não é possível confirmar o pedido.';
            WHEN 'REP' THEN
                P_SUCESSO := 'N';
                P_MENSAGEM := 'Oportunidade reprovada. Não é possível confirmar o pedido.';
            ELSE
                P_SUCESSO := 'S';
        END CASE;


    END;

END;