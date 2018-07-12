# -*- coding: utf-8 -*-
"""
@author: irah
"""

import pandas as pd
import numpy as np

import re
import random

from sklearn.feature_extraction import DictVectorizer
from sklearn.naive_bayes import BernoulliNB
from sklearn.linear_model import LogisticRegression
from sklearn.tree import DecisionTreeClassifier
from sklearn.cross_validation import KFold
from sklearn.metrics import f1_score, classification_report

#from nltk import pos_tag
import pickle
#import time


################# training #################

def train(data, classifier_file):# do not change the heading of the function
    cf = Feature_maker(data)
    model = Model_maker(cf.X_feat, cf.y_feat)
    pickle.dump( model, open( classifier_file, "wb" ) )
    return

################# testing #################

def test(data, classifier_file):# do not change the heading of the function
    tt = Feature_maker(data, blind=True)
    model = pickle.load( open( classifier_file, "rb" ) )
    out = model.get_all_predictions(tt, True)
    return out
    

##############################################################################
## Generate Features - Subfunctions
##############################################################################

class Feature_maker(object):
    
    def __init__(self, input_file, blind=False):
        
        self.blind = blind
        
        if isinstance(input_file, list):
            words = input_file    
        else:
            words = pd.read_csv(input_file, header=None)
            words = list(words[0])
        
        
        vowels =  '''
        AA, AE, AH, AO, AW, AY, EH, ER, EY, IH, IY, OW, OY, UH, UW
        '''
        self.vowels = set(re.findall('\w+', vowels, re.I))

        self.c_types = { 
                 '0001':'L', 
                 '0010':'2L', 
                 '1000':'F', 
                 '0100':'3L', 
                 
                 '010':'2L', 
                 '100':'F', 
                 '001':'L', 
                 
                 '01':'L', 
                 '10':'F'
                 }
       
        if blind:
            self.X_blind = self.make_blind_features(words)
        else:
            self.X_feat, self.y_feat = self.make_target_features(words)
        
        words = None
        

    ###########################################################################
    ## Generate Features - Subfunctions
    ###########################################################################        

    def get_target_features(self, phons):
        #nth from start
        phons = re.sub('\D', '', phons)
        phons = re.sub('[^1]', '0', phons)
        s = [i for i,t in enumerate(list(phons)) if t == '1']
        assert len(s) == 1
        s = s[0]+1
        
        n = len(phons)
        
        e = n - s + 1
        
        vow_type = self.c_types[phons]
        
        features = {'vow_cnt' : n,
                    'nth_from_start' : s,
                    'nth_from_end' : e,
                    'vow_str' : phons,
                    'vow_type': vow_type}
        
        return features
    
    
    def make_blind(self, phons):
        phons = re.sub('\d','',phons)
        return re.findall('\w+', phons)
    
    
    def get_token_features(self, tokens):
        features = set()
        vowels = self.vowels
        
        #count vowels
        n=0
        for t in tokens:
            if t in vowels:
                n+=1
        
        tokens = ['$']+tokens+['$']
        
        ##get prefix
#        pref = []
        i=0
        while i < len(tokens):
            if tokens[i] in vowels: break
            i+=1
        features.add("-".join(tokens[0:i+1]))
        features.add("-".join(tokens[0:i+2]))
#        features.add("-".join(tokens[0:i+3]))
    
        
        ## get 2nd (mid prefix)
        if n > 2:
            i += 1
#            j = i
            while i < len(tokens):
                if tokens[i] in vowels: break
                i+=1
            features.add('2-'+"-".join(tokens[i-1:i+1])+'-2')
            features.add('2-'+"-".join(tokens[i-1:i+2])+'-2')
#            features.add('2-'+"-".join(tokens[i-1:i+3])+'-2')
        
        ## get 3nd (mid prefix)
        if n > 3:
            i += 1
#            j = i
            while i < len(tokens):
                if tokens[i] in vowels: break
                i+=1
            features.add('3-'+"-".join(tokens[i-1:i+1])+'-3')
            features.add('3-'+"-".join(tokens[i-1:i+2])+'-3')
#            features.add('3-'+"-".join(tokens[i-1:i+3])+'-3')
    
        
        ##get suffix
#        suff=[]
        i=0
        tokens.reverse()
        while i < len(tokens):
            if tokens[i] in vowels: break
            i+=1
        suff = tokens[0:i+1]
        suff.reverse()
        features.add("-".join(suff))
#        
        suff = tokens[0:i+2]
        suff.reverse()
#        features.add("-".join(suff))
#
        suff = tokens[0:i+3]
        suff.reverse()
#        features.add("-".join(suff))
                
        return features, n
    
    
    def get_word_suffix_and_prefix(self, word):
        word = '$'+word+'$'
        features = set()
        
#        for i in [2,3]:
        for i in [2,3,4]:
#        for i in range(2,len(word)):
            features.add(word[0:i])
        
        word = word[::-1]
        
#        for i in [2,3]:
        for i in [2,3,4]:
#        for i in range(2,len(word)):
            features.add(word[0:i][::-1])
            
        return features


    def get_p_s_list(self, list_type):
#            suffix_priority = '''
#            -ion -ual -uous -ial -ient -ious -ior -ic -ity
#            -ist -ism -ize -ing
#            '''
#            suffix_other = '''
            
#            '''
##            prefix_priority = '''
#            a- ab- be- con- com- de- dis- e- ex- in- im-
#            per- pre-
#            '''
#            prefix_other = '''
#            ex- con- per- com- im- 
#            '''

            prefix_priority = 'TIK- YHO- OAX- ERU- EEV- NRO- AIG- EER- JOM- OUM- IKI- MOX- AA- EBY- EEN- ZOO- BYE- OTA- VEY- GUY- ZEM- OOS- OO- PLY- NSK- AER- OIS- EEM- ATI- AWM- AEV- AIS- SCS- SCE- NII- ZAL- YUM- EMA- ARG- QUE- ILM- ELF- OOP- UDH- BUL- KUK- EEP- ZIZ- OPP- RCI- EYN- KTU- SEE- YPH- IAP- APO- RSK- IUL- AI- OON- LGA- RAC- YUH- IBE- NSI- NCO- JAL- IEV- OYL- OUN- YOR- IV- AAM- RRI- BAB- TDO- OUL- OUX- NNO- ULY- RUN- OIN- UIZ- OIX- TTE- HUD- LEX- OOB- BOV- FLY- VEX- JAR- BIB- BEC- ENY- GJU- UAN- HOI- OUP- NGU- AAK- NTZ- LAC- NYI- UIE- BIO- AMI- JAB- OIT- ABA- DRI- DH- PAY- FOX- JFK- AO- SEI- POS- NEW- IQ- ARX- DRY- ECQ- VIP- AYA- UNO- NOL- REE- NAO- OIG- ANG- VED- QUN- FEI- OYU- VOW- IF- EEK- TAJ- LOO- EAU- AVI- RAG- EES- UEL- EFY- OUK- UEY- AYO- LEL- NEE- YM- DET- EAK- URS- IER- AUD- RUS- ESH- GEI- REG- UIS- SEA- OIE- LEB- GNS- RTU- SK- UIN- NEI- TOU- SHE- TAT- XEC- AIN- TAF- DAK- VSK- HAD- EAT- DAD- PHE- LAL- ZIM- EUR- RAT- EAS- RUG- IEN- ZOR- IRS- KOK- BU- DAO- PON- BOT- IBI- HID- TAX- KEI- ADE- NIR- NAM- AGU- RIP- JUR- ABO- RGO- VAR- DAT- OOL- ONA- CRY- BEY- UOT- WYK- ULT- HAK- LLE- UEN- EFT- OKE- ARR- AIT- YRE- AMU- YLS- LNA- RAP- NTY- GN- ERC- LUT- HIT- ONG- FFA- UIP- OYS- EIX- EDE- LTS- RUM- ATV- NST- IM- NNE- SP- XED- BAA- CUM- LIM- BED- EI- AMN- RAR- EGO- RAU- RAH- PUY- LOM- UNE- ERE- CTS- EAL- WAD- SOS- OAN- REY- LOU- LIG- NAT- AEL- AYS- MIT- OI- ILA- AHA- OUD- EED- HET- MOU- MAS- KGB- SHA- AUX- YEE- LLO- TET- GON- UCE- COE- AY- NOE- DU- UEZ- SED- EIT- VAH- RIE- GNE- CEK- OSS- PEE- BAI- RLY- UNK- DIL- BOL- CMX- ROT- OOR- FUR- SAF- IFS- UNG- SAT- ERK- IR- DOC- IEU- YED- DES- END- EAN- NDU- YEU-'

            prefix_other = 'DED- IC- CES- GES- BLE- GEL- LER- ZEL- NER- HUR- FUL- HER- TAD- EIM- GLE- ICS- DER- TLE- KER- HL- DLE- MER- TRE- USK- A- GER- ZES- CIL- SER- KET- PLE- BER- KLE- RF- WAN- FIN- PER- ODS- UIL- DY- VER- XES- SOR- SEN- PIL- CLE- LDE- IAN- DRE- SES- US- KEN- TEN- XIM- PID- COT- CHT- VIS- OV- SON- RLE- ZER- ERG- TED- KY- HLE- YER- LAD- FEL- PET- ACS- IE- BAT- O- CZ- AC- OMS- IG- JET- TTS- USH- EX- DOO- LDT- IFE- TO- BRE- TSE- KEL- OZ- IOT- ING- HY- SY- BY- SZ- NIS- LI- MY- UGE- LUX- GEN- SU- KO- MAN- POT- VID- JE- VAS- NOT- NIE- TY- VEE- NEY- CEE- RUD- FEE- I- GIL- SIS- UB- DL- GY- EXT- DUX- IK- EK- OFF- RER- DEN- SM- CO- PY- EDT- VET- NY- OLM- PEZ- XAR- KIN- LU- RO- BIT- NUE- CER- ORS- NOW- EC- RZ- ORP- MEE- RA- ERM- PH- OCH- ROR- NAD- HI- NNS- DIT- KON- HOE- TL- NTE- LZ- IRD- HR- ZEN- FTS- NAS- TKE- GIE-'
            
            suffix_priority = '-HEZ -AID -SMS -LTI -ITK -HEK -SIA -KUM -TEH -VOI -TUH -JOF -UFO -JEA -POG -HYP -WIT -GUA -GEU -EDR -OTH -TOD -HIE -NUE -IRE -NEV -ERB -URO -SUV -AWR -DVD -URE -OLA -SOS -EKE -RUJ -OSH -NIT -OLE -ERI -JUX -NG -JAP -ZER -VUI -ONA -PIO -GEV -HOI -WER -RIU -KAH -KAB -PUP -ZED -IDI -YEO -ELN -RIY -SRI -TIE -EBR -RYA -WAG -TYC -LOP -UPS -EIG -ILH -BBC -KM -OMA -ADC -SMI -LOL -EML -OSA -MEO -KON -KOR -YAR -NON -AGL -USU -IOU -ESQ -YUZ -ZIY -GWI -ULT -AKE -AYY -BIB -MIS -VIO -BUI -NIL -AKI -GON -EDS -EUG -LYO -ZUL -CUI -CIG -VOL -AZE -BOU -CYD -ORO -ASW -NOE -ASK -RHO -ROM -ETE -MUJ -MUT -WAZ -LC -ZAM -VIP -EGR -KVA -PUE -LIH -KEM -ASL -LSD -ASM -ILA -KUW -ATK -HSI -ADI -BOM -ORG -ZAL -HEV -XML -POU -NEP -LAJ -LYM -JAV -FEJ -PYL -KAC -OHE -JIA -FER -ULE -WEN -GUF -BIO -EDW -VP -NEG -ROQ -DAV -PIA -SED -CYA -AXF -GAZ -GOB -INT -CIP -BEQ -LH -OZE -WOT -GS -UMP -NID -LOI -UPH -KIB -AYO -MCV -PIM -PAM -BAP -GUJ -CEM -AVE -AUG -DIS -DZ -OVE -ROU -VIL -FOR -WOL -EPI -LLA -SEB -GAB -NAD -IRA -ABH -BEF -UN -LUO -OPP -HER -SLU -ISA -JON -TOK -OKA -GOU -LAF -AUD -HS -TER -BOI -COE -CUR -ODA -FUR -ARU -KG -SAR -ESP -JIR -RIA -SUP -HUG -BUF -YNE -THE -AFI -DYA -GAU -OMI -LIA -IDE -GAY -KRE -PIR -VAN -HAS -PEC -FUL -PER -TEL -QIR -PRC -ESK -HUV -NUM -OKE -OPI -GIR -CAB -ORA -HEL -FIF -SYL -SPE -LIV -CIR -SOT -COI -ERU -VEL -TAT -PEU -GEO -HJ -RIO -SYD -MIR -MOG -JER -TWA -PUR -SCS -ARI -IWA -PAB -JAM -DD -KUR -NGO -DOI -DEJ -BEG -SIT -NDA -AMA -UF -MC -OCO -AWA -KEN -ELK -RAP -KAZ -ATO -SAL -DUI'
            
            suffix_other = '-DEA -MCE -BOO -EA -AGN -MCI -REY -CRA -BY -REX -IGL -SUL -ELY -IV -OUE -OBJ -DEE -PEA -COB -EY -VIN -STO -SK -ENZ -SN -DEX -STR -BEE -MOO -FRI -BOA -AMP -AFS -BOT -CHL -EU -ECK -AHM -YA -OA -NOS -GOT -GAN -ALF -SV -DUG -COW -GUN -GOL -UPL -BAG -SUT -COG -FRU -BIR -HAN -SCH -SID -ENV -APH -AZM -MOH -FOJ -POW -KL -WHI -ROO -CLO -DIM -DIP -AI -LEI -DAL -COP -ANK -LIN -COO -SEI -AO -DEU -OX -EW -FOO -AIR -KN -LIG -DOG -ESM -DIT -ACT -DOO -VIS -MEM -SON -BUL -EMM -POC -OI -SQ -PAI -SEQ -PEP -AY -UPB -JOC -PHO -LAY -VES -EB -HOL -PIT -PAD -AGE -BUG -POM -BL -OVA -POO -TOP -PIG -ADL -SIG -SW -ARS -KAS -PRI -PL -DAS -ABJ -GAD -KO -SUE -DIE -SP -RY -HEA -WE -DRI -CU -OL -ELB -GUT -PT -HAI -IP -LIF -DW -AKB -PEE -ELV -LOV -ORC -AE -ZO -DAY -PHE -TAI -BOH -JOR -FIS -DY -UT -DRU -MEA -LUD -KIK -NOT -LAK -DOB -TEA -QUO -EI -APS -APO -MEE -BOX -HY -VET -INI -OZ -ART -ED -W -ELM -PED -OZA -NY -RAW -OT -BAI -HOU -DIB -STI -AHR -MOW -ISS -CAC -ROX -LIT -AMC -LUB -UD -VIT -DUD -VUL -GUS -ACK -PS -SES -NOW -LOW -ELL -KE -ALK -SEE -OS -RIV -LEP -OE -VO -MAF'
            
            
#            suffix_list = re.findall('\-\w+', suffix_priority + suffix_other)
#            prefix_list = re.findall('\w+\-', prefix_priority + prefix_other)
            
            
            suffix_priority = re.findall('\-(\w+)', suffix_priority)
            suffix_other = re.findall('\-(\w+)', suffix_other)
            
            
            prefix_priority = re.findall('(\w+)\-', prefix_priority)
            prefix_other = re.findall('(\w+)\-', prefix_other)
            
            out_list =  {
                        'suffix_priority': suffix_priority,
                        'suffix_other': suffix_other,
                        'prefix_priority':prefix_priority,
                        'prefix_other':prefix_other
                        }
            
            return set(out_list[list_type])




    def in_prefix_list(self, word, pref_list):
        for i in [1,2,3]:
            if word[:i] in pref_list:
                return True
        return False
            
    def in_suffix_list(self, word, suff_list):
        for i in [1,2,3]:
            if word[:i] in suff_list:
                return True
        return False



    
      
    ###########################################################################
    ## make features
    ###########################################################################
    
    
    def get_blind_feature_set(self, tokens, word):
        
        features = set()
        
        f, n = self.get_token_features(tokens)
        features.add('{}vls'.format(n))
        features.update(f)
        
        f = self.get_word_suffix_and_prefix(word)
        features.update(f)
        
        return(features, n)
    
    
    def make_target_features(self, words):
        
        X_feat = list()
        y_feat = list()
        
        for line in words:
            word, phons = line.split(':')
        
            line = dict()
#            features = set()
        
            ## TARGET FEATURES
            
            target = self.get_target_features(phons)
            
            target.update({'word':word, 'phons':phons})
            
            
            ## BLIND FEATURES
            
            tokens = self.make_blind(phons)
            
            features, n = self.get_blind_feature_set(tokens, word)

        
# dont need
#            if self.in_prefix_list(word, self.get_p_s_list('prefix_priority')):
#                features.update({'priority_pref'})
#                
#            elif self.in_prefix_list(word, self.get_p_s_list('prefix_other')):
#                features.update({'other_pref'})
#                
##            else:
##                features.update({'no_pref'})
#                
#            if self.in_suffix_list(word, self.get_p_s_list('suffix_priority')):
#                features.update({'priority_suff'})
#                
#            elif self.in_suffix_list(word, self.get_p_s_list('suffix_other')):
#                features.update({'other_suff'})
##            else:
##                features.update({'no_suff'})
# dont need?

        
            y_feat.append(target)
            X_feat.append(dict.fromkeys(features,1))
        
        return(X_feat, y_feat)
    
    
    def make_blind_features(self, words):
        X_blind = list()
        
        for line in words:
            word, phons = line.split(':')
        
            tokens = self.make_blind(phons)
                
            ## BLIND FEATURES
            
            features, n = self.get_blind_feature_set(tokens, word)
        
            line = {'n_vow': n,
                    'word': word,
                    'tokens': tokens,
                    'features': dict.fromkeys(features,1) }
        
            X_blind.append(line) 
        
        return(X_blind)
    


##############################################################################
## Make model
##############################################################################

class Model_maker(object):
    
    def __init__(self, X_feat, y_feat, debug=True):
        
        self.model=dict()
        self.debug = debug
        
        self.c_label = 'vow_type'
        
        self.c_types = ['L', '2L', 'F', '3L']
        
        
#                self.c_types = { 
#                 '0001':'L', 
#                 '0010':'2L', 
#                 '1000':'F', 
#                 '0100':'3L', 
#                 
#                 '010':'2L', 
#                 '100':'F', 
#                 '001':'L', 
#                 
#                 '01':'L', 
#                 '10':'F'
#                 }



        for t in self.c_types:
#            solver = "BernoulliNB()"
            solver = "LogisticRegression(C=8, tol=0.08, penalty='l1', solver='liblinear', class_weight='balanced')" #, class_weight='balanced'
#            solver = "DecisionTreeClassifier()"
            max_passes = 12
            
            debug or print('Training', solver, max_passes)
            
            #grow or not
#            X_feat_resample, y_feat_resample = list(), list()
            X_feat_resample, y_feat_resample = self.grow_misclass_instances(X_feat, y_feat, t, solver, max_passes, debug=debug)
            
            model_params = {'t_filter': t,
                            'X_feat': X_feat + X_feat_resample,
                            'y_feat': y_feat + y_feat_resample,
                            'solver': solver}
            
            debug or print('Making Model', t, len(X_feat + X_feat_resample))
            
            self.model[t] = self.make_model(**model_params)
        
        
        
    ##############################################################################
    ## Make Model
    ##############################################################################
    
    def make_model(self, **kwargs):
        
        debug       = kwargs.get('debug', True)
        X_feat      = kwargs.get('X_feat')
        y_feat      = kwargs.get('y_feat')
        t_filter    = kwargs.get('t_filter')
        solver      = kwargs.get('solver')
        
        debug or print(t_filter, solver)
        
        v = DictVectorizer(sparse=True, dtype=bool)
        TX = v.fit_transform(X_feat)
        
        #make solver
        sk = eval(solver)
        
        #convert y to feature vector
        Ty = [y[self.c_label] for y in y_feat]
        Ty = [(lambda t:True if t == t_filter else False)(y) for y in Ty]
        Ty = np.array(Ty, dtype='int64')
        
        sk.fit(TX, Ty)
            
        return {'v':v, 'sk':sk, 'TX':TX, 'Ty':Ty}

    ##############################################################################
    ## K-FOLD and grow Features from misclassified
    ##############################################################################
    
    def Kfolds_get_misclass(self, **kwargs):
        
        debug       = kwargs.get('debug', True)
        
        X_feat      = kwargs.get('X_feat')
        y_feat      = kwargs.get('y_feat')
        x_filter    = kwargs.get('x_filter')
        solver      = kwargs.get('solver')
        pass_n      = kwargs.get('pass_n', 1)
        
        misclass = list()
        
        debug or print('PASS:',pass_n,' ',solver, len(X_feat))
        
            
        v = DictVectorizer(sparse=True, dtype=bool)
        X = v.fit_transform(X_feat)
        
        # make solver
        sk = eval(solver)
        
        n_folds = 10
        kf = KFold(n = X.shape[0], n_folds = n_folds, shuffle = True) #, random_state = 42
        
        f1_total = 0
        
        #convert y to feature vector
        y = [y[self.c_label] for y in y_feat]
        y = [(lambda x:1 if x == x_filter else 0)(y) for y in y]
        y = np.array(y, dtype='int64')
        
        misclass_check = set()
        
        for train, test in kf:
            
            train_X, train_y = X[train], [y[i] for i in train]
            test_X, test_y = X[test], [y[i] for i in test]
            
            test_y_feat = [y_feat[i] for i in test]
            test_X_feat = [X_feat[i] for i in test]
            
            sk.fit(train_X, train_y)
            pred_y = sk.predict(test_X)
            
            f1 = f1_score( list(test_y), list(pred_y), average='binary')
            debug or print(sk.score(train_X, train_y), f1)
            f1_total += f1
            
            misclass += [(y, x) for i, j, y, x in zip(test_y, pred_y, test_y_feat, test_X_feat) if i!=j and y['word'] not in misclass_check]
            misclass_check.update([y['word'] for y, x  in misclass])
            misclass += [(y, x) for y,x in misclass if y==1] #only add positive
            
            
            f1_avg = f1_total/n_folds
            
        debug or print('average:', f1_avg, 'X:', len(X_feat), 'misclass:', len(misclass))
        
        
        X_misclass = [x for y,x in misclass]
        y_misclass = [y for y,x in misclass]    
            
            
        return( f1_avg, X_misclass, y_misclass )
    
    ##############################################################################
    
    
    def grow_misclass_instances(self, X_feat, y_feat, x_filter, solver, max_passes, debug=True):
        f1_avg_a = 0
        len_misclass_a = len(X_feat)
        pass_n = 1
        X_misclass_a = list()
        y_misclass_a = list()
        X_try = list()
        y_try = list()
        
        while True:
            params = {
                    'solver' : solver,
                    'X_feat' : X_feat + X_try + X_misclass_a,
                    'y_feat' : y_feat + y_try + y_misclass_a,
                    'x_filter' : x_filter,
                    'debug' : debug,
                    'pass_n' : pass_n }
            
            f1_avg_b, X_misclass_b, y_misclass_b = self.Kfolds_get_misclass(**params)
            if len_misclass_a < len(X_misclass_b):
#            if (f1_avg_b < f1_avg_a):
                break
            
            else:
                X_misclass_a += X_try
                y_misclass_a += y_try
                
                #boost
                X_try = X_misclass_b 
                y_try = y_misclass_b 
    
                f1_avg_a = f1_avg_b
                len_misclass_a = len(X_misclass_b)
                
                pass_n += 1
                
                if (pass_n > max_passes):
                    debug or print('n_max reached')
                    break
                
        return( X_misclass_a, y_misclass_a )
    

    ##############################################################################
    ## Extract Prediction
    ##############################################################################
    
    def get_prediction(self, X, nv):
#        debug = self.debug
        
        #test only for possible classes
#        t_range = range(1, nv+1)

        t_range = ({
                     4 : ['L', '2L', 'F', '3L'],
                     3 : ['2L', 'F', 'L'],
                     2 : ['L', 'F']
                 })[nv]

#                self.c_types = { 
#                 '0001':'L', 
#                 '0010':'2L', 
#                 '1000':'F', 
#                 '0100':'3L', 
#                 
#                 '010':'2L', 
#                 '100':'F', 
#                 '001':'L', 
#                 
#                 '01':'L', 
#                 '10':'F'
#                 }
        
        #get probability for each class
        y_poss = list()
        for t in t_range:
            
            sk = self.model[t]['sk']
            v = self.model[t]['v']
            
            vX = v.transform(X)
            
            c = sk.predict(vX).tolist()[0]
            p = sk.predict_proba(vX).tolist()[0][1]
            np = sk.predict_proba(vX).tolist()[0][0]
            y_poss += [(t, c, p, np)]
        
#        y_poss.sort(key=lambda x:x[3])
#        debug or print(y_poss)
        #get class with max probability
        t_max, _c, _p, _np = max(y_poss, key=lambda x:x[2])
        
        return(t_max)
            
    
    def get_all_predictions(self, ft, transform = False):
#        debug = self.debug
        
        y_pred = list()
        for X in ft.X_blind:
            X_feat = X['features']
            nv = X['n_vow']
#            debug or print( X['word'] )
            
            y = self.get_prediction(X_feat, nv)
            
            if transform: y = self.transform_prediction(nv, y)
            y_pred.append(y)
        
        return(y_pred)
    
    
    def transform_prediction(self, nv, y_pclass):
        t_trans_dict = {
                    4 : {'L': 4, '2L': 3, 'F': 1, '3L': 2},
                    3 : {'2L': 2, 'F': 1, 'L': 3},
                    2 : {'L': 2, 'F': 1}
                    }
        
#                self.c_types = { 
#                 '0001':'L', 
#                 '0010':'2L', 
#                 '1000':'F', 
#                 '0100':'3L', 
#                 
#                 '010':'2L', 
#                 '100':'F', 
#                 '001':'L', 
#                 
#                 '01':'L', 
#                 '10':'F'
#                 }
        return t_trans_dict[nv][y_pclass]
    

    def  rate_n_random_predictions(self, ft, n_random, transform=False):
        
        if transform == False:
            target_type = self.c_label
        else:
            target_type = 'nth_from_start'
        
        print('Testing')
        X_feat = ft.X_feat[:]
        y_feat = ft.y_feat[:]
        y_target = [y[target_type] for y in y_feat]
        n_vowels = [y['vow_cnt'] for y in y_feat]
        
        i_list = list(range(len(X_feat)))
        random.shuffle(i_list)
        i_list = i_list[:n_random]
        y_known=list()
        
        y_pred = list()
        
        # get predictions
        for i in i_list:
            X = X_feat[i]
            nv = n_vowels[i]
            y_known.append(y_target[i])
            y_pclass = self.get_prediction(X, nv)
            y_trans = self.transform_prediction(nv, y_pclass)
            
            if transform:
                y_p = y_trans
            else:
                y_p = y_pclass
                
            y_pred.append(y_p)
            
#            if y_trans != y_target[i]:
#                print(y_feat[i]['word'], ':', y_feat[i]['phons'], 
#                      'target:', y_feat[i][self.c_label], y_feat[i]['nth_from_start'],
#                      'classed:', y_pclass, y_trans)
                
        
        #print(y_target, y_pred)
        print('f1_score:', f1_score(y_known, y_pred, average='macro'))
        print(classification_report(y_known, y_pred))
        
                
