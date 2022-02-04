create or replace PROCEDURE "STP_EVT_ITE_ESPECIE" (
       P_TIPOEVENTO INT,    -- Identifica o tipo de evento
       P_IDSESSAO VARCHAR2, -- Identificador da execução. Serve para buscar informações dos campos da execução.
       P_CODUSU INT         -- Código do usuário logado
) AS
       BEFORE_INSERT INT;
       AFTER_INSERT  INT;
       BEFORE_DELETE INT;
       AFTER_DELETE  INT;
       BEFORE_UPDATE INT;
       AFTER_UPDATE  INT;
       BEFORE_COMMIT INT;
       V_NUNOTA      INT;
       V_COUNT       INT;
       V_I           INT;
       V_SEQUENCIA   INT;
       V_ESPECIE_VOL_AUX VARCHAR2(4000);
       V_ESPECIE_VOL VARCHAR2(4000);
       V_ESPECIE_VOL_ANT VARCHAR2(4000);
       V_PREENCESPAUT VARCHAR2(4000);
BEGIN
       BEFORE_INSERT := 0;
       AFTER_INSERT  := 1;
       BEFORE_DELETE := 2;
       AFTER_DELETE  := 3;
       BEFORE_UPDATE := 4;
       AFTER_UPDATE  := 5;
       BEFORE_COMMIT := 10;
       V_I           := 1;
       V_ESPECIE_VOL_ANT := '';
       
/*******************************************************************************
   É possível obter o valor dos campos através das Functions:
   
  EVP_GET_CAMPO_DTA(P_IDSESSAO, 'NOMECAMPO') -- PARA CAMPOS DE DATA
  EVP_GET_CAMPO_INT(P_IDSESSAO, 'NOMECAMPO') -- PARA CAMPOS NUMÉRICOS INTEIROS
  EVP_GET_CAMPO_DEC(P_IDSESSAO, 'NOMECAMPO') -- PARA CAMPOS NUMÉRICOS DECIMAIS
  EVP_GET_CAMPO_TEXTO(P_IDSESSAO, 'NOMECAMPO')   -- PARA CAMPOS TEXTO
  
  O primeiro argumento é uma chave para esta execução. O segundo é o nome do campo.
  
  Para os eventos BEFORE UPDATE, BEFORE INSERT e AFTER DELETE todos os campos estarão disponíveis.
  Para os demais, somente os campos que pertencem à PK
  
  * Os campos CLOB/TEXT serão enviados convertidos para VARCHAR(4000)
  
  Também é possível alterar o valor de um campo através das Stored procedures:
  
  EVP_SET_CAMPO_DTA(P_IDSESSAO,  'NOMECAMPO', VALOR) -- VALOR DEVE SER UMA DATA
  EVP_SET_CAMPO_INT(P_IDSESSAO,  'NOMECAMPO', VALOR) -- VALOR DEVE SER UM NÚMERO INTEIRO
  EVP_SET_CAMPO_DEC(P_IDSESSAO,  'NOMECAMPO', VALOR) -- VALOR DEVE SER UM NÚMERO DECIMAL
  EVP_SET_CAMPO_TEXTO(P_IDSESSAO,  'NOMECAMPO', VALOR) -- VALOR DEVE SER UM TEXTO
********************************************************************************/

/*     IF P_TIPOEVENTO = BEFORE_INSERT THEN
             --DESCOMENTE ESTE BLOCO PARA PROGRAMAR O "BEFORE INSERT"
       END IF;*/
       
       
       
       IF P_TIPOEVENTO IN( AFTER_INSERT,  AFTER_UPDATE) THEN
         
            
            V_NUNOTA := EVP_GET_CAMPO_INT(P_IDSESSAO, 'NUNOTA');
            
            IF V_NUNOTA IS NOT NULL THEN
            
                SELECT TOP.AD_PREENCESPAUT
                INTO V_PREENCESPAUT
                FROM TGFTOP TOP
                WHERE TOP.CODTIPOPER = (SELECT CODTIPOPER FROM TGFCAB WHERE NUNOTA = V_NUNOTA)
                AND TOP.DHALTER = (SELECT MAX(DHALTER) FROM TGFTOP WHERE CODTIPOPER = TOP.CODTIPOPER);               
                
                IF V_PREENCESPAUT = 'S' THEN 
                
                    SELECT COUNT(*)--, PRO.CODPROD
                    INTO V_COUNT
                    FROM TGFPRO PRO JOIN
                    TGFITE ITE ON PRO.CODPROD = ITE.CODPROD
                    WHERE ITE.NUNOTA = V_NUNOTA;  
                    
                    --raise_application_error(-20111,'Teste2'|| ' ' || V_NUNOTA || ' ' || V_ESPECIE_VOL || ' ' || V_ESPECIE_VOL_ANT|| ' ' || V_COUNT || ' ' || V_I );
                    
                    FOR V_I IN 1..V_COUNT LOOP    
                    
                    --raise_application_error(-20111,'Teste2'|| ' ' || V_NUNOTA || ' ' || V_ESPECIE_VOL || ' ' || V_ESPECIE_VOL_ANT|| ' ' || V_COUNT || ' ' || V_I );
                        
                        BEGIN                    
                            SELECT PRO.AD_ESPECIE
                            INTO V_ESPECIE_VOL
                            FROM TGFPRO PRO JOIN
                            TGFITE ITE ON PRO.CODPROD = ITE.CODPROD
                            WHERE ITE.NUNOTA = V_NUNOTA
                            AND ITE.SEQUENCIA = V_I;
                        EXCEPTION 
                        WHEN NO_DATA_FOUND THEN
                            V_ESPECIE_VOL := V_ESPECIE_VOL;
                        END;    
                        
                        --raise_application_error(-20111,'Teste2'|| ' ' || V_NUNOTA || ' ' || V_ESPECIE_VOL || ' ' || V_ESPECIE_VOL_ANT|| ' ' || V_COUNT || ' ' || V_I );
                    
                        IF (V_ESPECIE_VOL_ANT = V_ESPECIE_VOL) OR (V_I = 1) THEN 
                            V_ESPECIE_VOL_ANT := V_ESPECIE_VOL;
                        ELSE 
                            V_ESPECIE_VOL_ANT := 'DIVERSOS';
                        END IF;
                        
                        UPDATE TGFCAB SET VOLUME = V_ESPECIE_VOL_ANT WHERE NUNOTA = V_NUNOTA;
                        --raise_application_error(-20111,'Teste2'|| ' ' || V_NUNOTA || ' ' || V_ESPECIE_VOL || ' ' || V_ESPECIE_VOL_ANT|| ' ' || V_COUNT || ' ' || V_I );
                    END LOOP;
                                        
                    --UPDATE TGFCAB SET VOLUME = V_ESPECIE_VOL WHERE NUNOTA = V_NUNOTA;
                    
                END IF;        
                
            END IF;
       END IF;
       
       
       IF P_TIPOEVENTO = AFTER_DELETE  THEN
         
            V_NUNOTA := EVP_GET_CAMPO_INT(P_IDSESSAO, 'NUNOTA');
            V_SEQUENCIA := EVP_GET_CAMPO_INT(P_IDSESSAO, 'SEQUENCIA');

            IF V_NUNOTA IS NOT NULL THEN
            
                SELECT TOP.AD_PREENCESPAUT
                INTO V_PREENCESPAUT
                FROM TGFTOP TOP
                WHERE TOP.CODTIPOPER = (SELECT CODTIPOPER FROM TGFCAB WHERE NUNOTA = V_NUNOTA)
                AND TOP.DHALTER = (SELECT MAX(DHALTER) FROM TGFTOP WHERE CODTIPOPER = TOP.CODTIPOPER);               
                
                IF V_PREENCESPAUT = 'S' THEN 
                
                    --raise_application_error(-20111,'Teste_EXC'|| ' ' || V_NUNOTA || ' ' || V_ESPECIE_VOL || ' ' || V_ESPECIE_VOL_ANT|| ' ' || V_COUNT || ' ' || V_I || ' ' || V_SEQUENCIA);
                
                    SELECT COUNT(*)--, PRO.CODPROD
                    INTO V_COUNT
                    FROM TGFPRO PRO 
                    JOIN TGFITE ITE ON PRO.CODPROD = ITE.CODPROD
                    WHERE ITE.NUNOTA = V_NUNOTA; 
                    
                    IF V_COUNT > 1 THEN
                    
                        --raise_application_error(-20111,'Teste2'|| ' ' || V_NUNOTA || ' ' || V_ESPECIE_VOL || ' ' || V_ESPECIE_VOL_ANT|| ' ' || V_COUNT || ' ' || V_I );
                        
                        FOR V_I IN 1..V_COUNT LOOP   
                        
                        
                            V_SEQUENCIA := EVP_GET_CAMPO_INT(P_IDSESSAO, 'SEQUENCIA');
                        
                        --raise_application_error(-20111,'Teste2'|| ' ' || V_NUNOTA || ' ' || V_ESPECIE_VOL || ' ' || V_ESPECIE_VOL_ANT|| ' ' || V_COUNT || ' ' || V_I || ' ' || V_SEQUENCIA );
                            
                            BEGIN                
                                SELECT PRO.AD_ESPECIE
                                INTO V_ESPECIE_VOL
                                FROM TGFPRO PRO JOIN
                                TGFITE ITE ON PRO.CODPROD = ITE.CODPROD
                                WHERE ITE.NUNOTA = V_NUNOTA
                                --AND ITE.SEQUENCIA NOT IN ( V_SEQUENCIA)
                                AND ITE.SEQUENCIA = V_I;
                            EXCEPTION 
                            WHEN NO_DATA_FOUND THEN
                                V_ESPECIE_VOL_AUX := V_ESPECIE_VOL;
                            END;
                            
                            --raise_application_error(-20111,'Teste2'|| ' ' || V_NUNOTA || ' ' || V_ESPECIE_VOL || ' ' || V_ESPECIE_VOL_ANT|| ' ' || V_COUNT || ' ' || V_I );
                        
                            IF (V_ESPECIE_VOL_ANT = V_ESPECIE_VOL) OR (V_I = 1) THEN                            
                                V_ESPECIE_VOL_ANT := V_ESPECIE_VOL;
                            ELSE 
                                --raise_application_error(-20111,'Teste2'|| ' ' || V_NUNOTA || ' ' || V_ESPECIE_VOL || ' ' || V_ESPECIE_VOL_ANT|| ' ' || V_COUNT || ' ' || V_I );
                        
                                V_ESPECIE_VOL_ANT := 'DIVERSOS';
                            END IF;
                            
                            UPDATE TGFCAB SET VOLUME = V_ESPECIE_VOL_ANT WHERE NUNOTA = V_NUNOTA;
                            --raise_application_error(-20111,'Teste2'|| ' ' || V_NUNOTA || ' ' || V_ESPECIE_VOL || ' ' || V_ESPECIE_VOL_ANT|| ' ' || V_COUNT || ' ' || V_I );
                        END LOOP;                                       
                    
                    ELSE 
                    
                        SELECT PRO.AD_ESPECIE
                        INTO V_ESPECIE_VOL
                        FROM TGFPRO PRO JOIN
                        TGFITE ITE ON PRO.CODPROD = ITE.CODPROD
                        WHERE ITE.NUNOTA = V_NUNOTA
                        --AND ITE.SEQUENCIA NOT IN ( V_SEQUENCIA)
                        --AND ITE.SEQUENCIA = V_I
                        ;
                      --   raise_application_error(-20111,'Teste2'|| ' ' || V_NUNOTA || ' ' || V_ESPECIE_VOL || ' ' || V_ESPECIE_VOL_ANT|| ' ' || V_COUNT || ' ' || V_I );
                        UPDATE TGFCAB SET VOLUME = V_ESPECIE_VOL WHERE NUNOTA = V_NUNOTA;
                    END IF;
                END IF;        
                
            END IF;
            
       END IF;     
            
            
/*     IF P_TIPOEVENTO = BEFORE_DELETE THEN
             --DESCOMENTE ESTE BLOCO PARA PROGRAMAR O "BEFORE DELETE"
       END IF;*/
/*       IF P_TIPOEVENTO = AFTER_DELETE THEN
             --DESCOMENTE ESTE BLOCO PARA PROGRAMAR O "AFTER DELETE"
       END IF;*/

/*     IF P_TIPOEVENTO = BEFORE_UPDATE THEN
             --DESCOMENTE ESTE BLOCO PARA PROGRAMAR O "BEFORE UPDATE"
       END IF;*/
/*     IF P_TIPOEVENTO = AFTER_UPDATE THEN
             --DESCOMENTE ESTE BLOCO PARA PROGRAMAR O "AFTER UPDATE"
       END IF;*/

/*     IF P_TIPOEVENTO = BEFORE_COMMIT THEN
             --DESCOMENTE ESTE BLOCO PARA PROGRAMAR O "BEFORE COMMIT"
       END IF;*/

END;