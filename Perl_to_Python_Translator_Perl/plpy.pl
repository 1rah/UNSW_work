#!/usr/bin/perl -w

##############################################################################
# COMP9041 Assignment 1, Irah Wajchman, z3439745

##############################################################################
## set global variables ##
$totLnIn = 0;
%lDef = ();
%imp =();


##############################################################################
## Driver ## ----------------------------------------------------------------
#~cs2041/bin/autotest plpy
$F = readIn();
if (!defined $F) {die;};
@F = ReFlow($F); # perform first pass cahnges
@F = postFilter(@F); # apply second pass changes

# Construct output
print "#!/usr/local/bin/python3.5 -u\n\n";
#print "#!/usr/bin/python3 -u\n\n";
print join "\n", "#imports", (keys %imp),"\n" if ((scalar (keys %imp)) gt 0); 
print join "\n", "#initialisations", (keys %lDef), "\n" if ((scalar (keys %lDef)) gt 0);
print join "\n",@F,"\n";




##############################################################################

##############################################################################
## INPUT STRING HANDLING FUNCTIONS -------------------------------------------

## reads in a string, a splits off the head at the specified character
## the input string is modified in place, the head is returned
sub readIn {
  my @t=();
  while (my $line = <>){

      chomp $line;
      $line =~ s/^\s+//g; #reduce all white space to 1   
      push @t, $line;
      $totLnIn = $.;
  }
  my $t = join "\n", @t;
  return $t;
}

## split head ($h) direct from input string (${st}) at delimiter ($r)
sub getH (\$$){
  my ($h, $st, $r);
  ($st, $r) = @_;
  ($h, ${$st}) = split /$r/, ${$st}, 2;
  return "$h" if defined $h;
  return "";
}


###############################################################################
## STRING / EVAL FUNCTIONS ----------------------------------------------------


# convert an input string depending on it's type
sub chSnglStr {

  my ($l) = @_;

  $l =~ s/^\s+//;

  # if a raw string, return as is
  if ($l =~ /^\'/) {
      return $l;
    }

  # if a formatted string, format variables
  if ($l =~ /^\"/) {
    $l = strFormat($l);
    return $l          
    }

  if ($l =~ /^\@/) {
    $l = 'join"",'.$l;
    }

  if ($l =~ /^join/) {
    $l = chJoin($l);
    return $l          
    }

#  # if not converted 
#  return 'notCon chSnglStr:'.$l[0].'='.$l[1]
    return $l;   
}


# returns a string, as "".format(list) type,
# with variables in a list
sub strFormat{
  my ($l) = @_;
#  print "*$l*";
  
  # if variables inside a string, strip $ and add to @vars
  if ($l =~ /\$+/) {
      
    my @vars=();
    #list and hashes
    push @vars, $1 while ($l =~ s/(\$\w[\w\d]*[\{\[].*[\}\]])/{}/g);

    #single scalar
    push @vars, $1 while ($l =~ s/(\$\w[\w\d]*)/{}/g);

    return $l.'.format(' . join(", ", @vars) . ')';
  
  }else{
    return $l;
   }
}


# convert a perl join,'c',list to 
# python "c".join(list)
sub chJoin{
  my($l) = @_;
#  print"!$l!";
  $l =~ s/join//;
  $l =~ s/[\(\)]//g;

#  my(@l) = split /\,/,$l;
#  $l = shift @l;
  
    my $c = getH($l,'(?=.)');

    my $s = getH($l,$c);

    $c = getH($l,'(?=.)');
#  print "*$l*";
    $l =~ s/^\s//;
  if ($l =~ /^\@/ ) {
    return '"'.$s.'".join('.$l.')';
    }
  else {
    return '"'.$s.'".join(['.$l.'])';
  }
#  return $l . '.join(' . join(",",@l) . ')';
}


###############################################################################
## SINGLE SCALAR VARIABLE ASSIGNMENTS ----------------------------------------

# subfunction for splitting the variables from the assigned sting
# returing the string in python format

sub chSgVarAss {
  my ($l) = @_;

  
  @l = split /\=/, $l, 2;
#  print "!$l[1]!";  
#  $l[0] =~ s/\$//;
  
  return $l[0].' = '.chSnglStr($l[1]);

  # if not converted 
  return 'notCon chSgVarAss:'.$l[0].'='.$l[1]

}


###############################################################################
## PRINTING FUNCTIONS --------------------------------------------------------

# convert a perl print string to a pythonprint string
# by reforamtting all strings to "".format() type
sub chPrintLst {
  my ($l) = @_;

  
  $l =~ s/^print\s*//;
  $l =~ s/^\(//;
  $l =~ s/\)$//;
  
  # split on commas not enclosed in parenthesis
  my @l = split  /(?![^(]+\)), /, $l;
  my @out =();
     
  foreach $m (@l) {
#    print "*$m*";
    #if line is a string with variables, then expand them
    $m = chSnglStr($m);
    $m = 'print('.$m.', end="", sep="")';
    push @out, $m;
    }
  return join "\n", @out;
}




###############################################################################
## FLOW CONTROL STATEMENTS ---------------------------------------------------

# take the heading of a flow control statement,
# then recursively return its internal loop, add white space to the front of it
sub chFlwCntrl (\$$){
  
  my ($st, $type) = @_;
  my (@out, @while) =();
  my $h="";

  getH(${$st},'(?=\()');
  $h = getH(${$st},'\{');

  $h = $type.$h.":";

  push @out ,$h;

  # recursively convert the contents of the loop
  (${$st}, @while) = ReFlow(${$st});
  foreach (@while) {push  @out, "  ".$_ ;}
  
  return @out;
}


# return inner loop for an else command while adding space
sub chFlwElse (\$$){
  
  my ($st, $type) = @_;
  my (@out, @while) =();
  my $h="";

  push @out, $type.":";      
  
  # recursively convert the contents of the loop
  getH(${$st},'\{');
  (${$st}, @while) = ReFlow(${$st});
  foreach (@while) {push  @out, "  ".$_ ;}
  
  return @out;
}


# take the heading of a for / foreach loop,
# then recursively return its internal loop, add white space to the front of it
sub chForLp (\$$){
  
  my ($st, $type) = @_;
  my (@out, @while) =();
  my ($h, $loop, $var)="";

  # trim the for condiction, and get scalar loop variable      
  $h = getH(${$st},'\(');
  $h =~ s/$type//;
  $h =~ s/\s*//;
  $h =~ s/\s*$//;


  # if no variable specified, assign anonymous variable
  if ($h =~ /\$\w[\w\d]*/) {
    $var = $h;
    }
  else {
    $var ='$_';
  }

  # trim the list to iterate on
  $h = getH(${$st},'\{');
  $h =~ s/\)\s*$//;
 

  ## reconstruct the loop statement depending on type

  ## change "for (;;)" to "while (1):"  
  if ($h =~ /;;/) {
    $h = 'while (1):';
    }
  
  # change "for $v (x..y)" to "for v in range(x,y):"
  elsif ($h =~ /([^.]+)\.\.([^.]+)/) {
    $h = "for $var in range(".$1.','.$2.'+1):';
    }

  # change "for $v (x;cond;x++)" to "x=0, while (cond): x++ ..."  
  elsif ($h =~ /([^\;]+)\;([^\;]+)\;([^\;]+)/) {
    my ($c1, $c2, $c3) = ($1, $2, $3);
    $c2 =~ s/\s+//g;
    $c1 =~ s/\s+//g;
    $c3 =~ s/\s+//g;
    $h = $c1.'-1'."\n".'while('.$c2.'-1):'."\n  ".$c3;
    }
  
  # change "for $var (list)" to "for var in list:"
  else {
    $h = 'for '.$var.' in ('.$h.'):';
    }
  
  push @out, (split "\n",$h);      
  
  # recursively convert the contents of the loop, adding whiespace
  (${$st}, @while) = ReFlow(${$st});
  foreach (@while) {push  @out, "  ".$_ ;}
  
  return @out;
}



###############################################################################
## REFLOW: --------------------------------------------------------------------

# used to recursively reflow the input string based on the leading text
# the head is recursively plucked off the input string and then fed into
# the @out list
# depending on the start of the string differenct converstion / recursions are 
# applied

sub ReFlow {
  my ($st) = @_;
  my $h = "";

  my $cnt = 0;
  my @out = ();
  
  while ($st) {
  
#    print "ST $cnt: $st\n";
#    $cnt++;

    #if blank line, return blank
    if ( $st =~ /^\n\n+/ ) { getH($st,"\n"); push @out, ""; }
    
    #remove any leading whitespace
    if ( $st =~ /^\s+/ ) { $st =~ s/^\s+//; }


    # exit condition, end of nested function
    if ($st =~ /^\}/) {
      $st =~ s/^\}//;
      last;
      }
    
    #read in #! line, and remove
    if ( $st =~ /^\#\!\/usr\/bin\/perl -w/ ) {
      getH($st,"\n");
      #push @out, "#!/usr/local/bin/python3.5 -u";
      next;
      }
    
    #read in any body comments
    if ( $st =~ /^#/ ) {
      push @out, getH($st,"\n");
      next;
       }
    
    #read in next command
    if ( $st =~ /^next/ ) {
      getH($st,';');
      push @out, 'continue';
      next;
      }

    #read in last command
    if ( $st =~ /^last/ ) {
      getH($st,';');
      push @out, 'break';
      next;
      }
        
    
    ## convert while loop
    if ( $st =~ /^while/ ) {
      push @out, chFlwCntrl($st,'while');
      next;
      }

    ## convert if function
    if ( $st =~ /^if/ ) {
      push @out, chFlwCntrl($st,'if');
      next;
      }

    ## convert if function
    if ( $st =~ /^elsif/ ) {
      push @out, chFlwCntrl($st,'elif');
      next;
      }

    ## convert if function
    if ( $st =~ /^else/ ) {
      push @out, chFlwElse($st,'else');
      next;
      }

    ## convert foreach function
    if ( $st =~ /^foreach/ ){
        push @out, chForLp($st,'foreach');
        next;
        }

    ## convert for function
    if ( $st =~ /^for/ ){
        push @out, chForLp($st,'for');
        next;
        }


    #read in any print commands !OLD!
    if ( $st  =~ /^print[\s+|\(|\"]/ ) {
      push @out, chPrintLst(getH($st,"[;|\n]"));
      next;
      }

    #convert assign $x = <STDIN>;
    if ( $st =~ /^\$[^\=]*\=\s*<STDIN>/) {
      
      $h = getH($st,";");
      $h =~ s/(\$[^\=]*)\=\s*<STDIN>/$1/g;
      push @out, ("$h= input()", "try: $h = float($h)", "except: pass");
      next;
     }


    #read in string assignment argument
    if ( $st =~ /^[\S]+[^\=\<\>\!\+\~]\=[^\=\<\>\!\+\~][^\;\}\{]+;/ ) {
      $h = getH($st,";");
      $h = chSgVarAss($h);
      push @out,$h;
      next;
      }


    ## return unmatched lines #
    if ($st =~ /.+?\;/) {
      push @out, getH($st,";");
      next;
      }
      
    
    ## handle any remaining lines
    if ($st =~ /^\S+/) {
      push @out, "!notCon Main ln $.: |". getH($st,";").'|';
      next;
      }

  }
  return $st, @out;     
}


###############################################################################
# Sub-Fucntions for PostFilter()

# Change the format of a scalar var
sub chSclr{
  my ($l) = @_;
  $l =~ s/\$(\w[\w\d]*)/$1/g;
  return $l;
}

# chance a qw() list, to a list
sub chqw{
  my ($l) = @_;
  my (@vars) = split " ", $1;

  return '["'.join('","', @vars).'"]';
}

# subfuntion used in chPush(),
# returns a list name, and its variables 
sub chListOp{
  my ($l) = @_;
  
  my ($list, $vars) = split("(?=,)", $l, 2);
  
  my @vars =();
  push (@vars, "$1") while ( $vars =~ /,([^\,]+)/g );  
  
  $list =~ s/\s//g;
  
  return ($list, @vars);
}



#convert the push and unshift commands
sub chPush{
  my ($l) = @_;

  # convert "push,list,var" to "list.append(var)"  
  if ($l =~ /^push/){ 
    $l =~ s/^push//;
    my ($list, @vars)= chListOp($l);
    return '['.$list.'.append(i) for i in('.join(',',@vars).',)]';
    }

  # convert "unshift,list,var" to "list.insert(0,ar)"    
  if ($l =~ /^unshift/) {
    $l =~ s/^unshift//;
    my ($list, @vars)= chListOp($l);
    return '['.$list.'.insert(0,i) for i in('.join(',',@vars).',)]';
    }
       
  return
}


# convert a "split,c,string" command to "re.split("c",strint)"
sub chSplit {
  my ($l) = @_;

  $l =~ s/split\s*//;

    my $c = getH($l,'(?=.)');

    my $s = getH($l,$c);

    $c = getH($l,'(?=.)');

    $imp{'import re'}=1;

  return 're.split(r\''.$s.'\', '.$l.')';
}



##############################################################################
# ----------------------------------------------------------------------------
# POST FILTER - APPLY IN_PLACE CHANGES (after ReFlow() )

sub postFilter {
  my (@in) = @_;
  my @out = ();
  my @matches = ();

  foreach my $i (@in) {
    
    # check if line is emty or a comment line
    if (! defined $i) {push @out, ""; next;}
    if ($i =~ /^\s+$/) {push @out, $i; next;}
    if ($i =~ /^#/) {push @out, $i; next;}


    #### Start In place Conversions ###
  
    # while ($line = <STDIN>) to for line in sys.stdin:
    if ($i =~ /while\s*\(\s*.+?\s*=\s*<\s*STDIN\s*>\s*\)/) {
     $i =~ s/while\s*\(\s*(.+?)\s*=\s*<\s*STDIN\s*>\s*\)/for $1 in sys.stdin/;
     $imp{'import sys'}=1;
    }
    
    # while ($line = <>) to for line in fileinput.input()
    if ($i =~ /while\s*\(\s*.+?\s*=\s*<\s*>\s*\)/) {
     $i =~ s/while\s*\(\s*(.+?)\s*=\s*<\s*>\s*\)/for $1 in fileinput.input()/;
     $imp{'import fileinput'}=1;
    }
    
    #convert chomp arg
    $i =~ s/chomp\(*\s+([^\s]+)\)*/$1 = $1.rstrip("\\n")/;
            
    # logical comparison changes
    $i =~ s/\beq\b/==/g;
    $i =~ s/\blt\b/</g;
    $i =~ s/\bgt\b/>/g;
    $i =~ s/\&\&/and/g;
    $i =~ s/\|\|/or/g;
    $i =~ s/\b![^=]\b/not/g;


    # convert a simple regular expression
    if ($i =~ /(.*)=~\s*s\/(.*)\/(.*)\/(\w*)/) {
      $i =~ s/(.*)=~\s*s\/(.*)\/(.*)\/(\w*)/$1= re.sub(r'$2', '$3',$1)/;
      $imp{'import re'}=1;
      }

    # convert a split statement 
    $i =~ s/(split.*)/chSplit($1)/ge;
    


    # replace list names @ with a_ and create initialisation line
    push (@matches, "$1") while ($i =~ s/\@(\w[\w\d]*)/a_$1/);
    while (@matches) {
      $m = shift @matches;  
      if ( $m =~ /ARGV/ ) {
        $imp{'import sys'}=1;
        $lDef{'a_ARGV = sys.argv[1:]'}=1;
        }
      else {
        $lDef{"a_$m = list()"}=1;
        }
    }

    # replace hash names % with d_ and create initialisation line
    push (@matches, "$1") while ( $i =~ s/\%(\w+[\w\d]*)/d_$1/ );
    while (@matches) {
        $m = shift @matches;
        $imp{'from collections import defaultdict'}=1;        
        $lDef{"d_$m = defaultdict()"}=1;
        }
    
    # reset a list
    $i =~ s/(a_[\w\d]+)\s*\=\s*\(\)/$1 = list()/;
    
    # reset a dict
    $i =~ s/(d_[\w\d]+)\s*\=\s*\(\)/$1 = defaultdict()/;


    ## LIST OPERATIONS   
    
    # convert a push or shift command
    $i =~ s/((push|unshift)[^\,]+(,[^\,]+)+)/chPush($1)/ge;
    
    # pop goes to list.pop()
    $i =~ s/pop[\(|\s+](a_\w+)[\)|\s]*/$1.pop()/;

    # shift goes to list.pop(0)
    $i =~ s/pop[\(|\s+](a_\w+)[\)|\s]*/$1.pop(0)/;

    # remove my
    $i =~s/(my)[\s|\(]//g;
    
    # convert $#list to len(a_list)
    $i =~ s/\$\#([\w\d]+)/len(a_$1)-1/g;




    # handle $_ ??
    # change list scalar variable name
    $i =~ s/\$([\w\d]+)\[(.+?)\]/a_$1\[$2\]/g;
    
    # convert a dictionary scalar variable name
    $i =~ s/\$([\w\d]+)\{(.+?)\}/d_$1\[$2\]/g;

    # convert a scalar variable name
    $i =~ s/\$([\w\d]+)/$1/g;

    # change qw to list
    $i =~ s/qw\((.*?)\)/chqw($1)/ge;

    # change scalar incremeters (++, --)
    $i =~ s/\+\+/\+\=1/g;
    $i =~ s/\-\-/\-\=1/g;

    # change exit 0 to sys.exit(1)
    if ($i =~ /exit 0/){
      $i =~ s/exit 0/sys.exit(1)/g;
      $imp{'import sys'}=1;
      }

    # initalise ARGV list  
    if ( $i =~ /ARGV/ ) {
        $imp{'import sys'}=1;
        $lDef{'a_ARGV = sys.argv[1:]'}=1;
        }

 
    push @out, $i;
  }
  return (@out);
}






# END  -----------------------------------------------------------------------
##############################################################################

