#!/usr/bin/perl
use strict;

my $dir = './';

my @lines;

my %xmap;


foreach my $fn (@ARGV) {
    $dir = $fn;
    $dir =~ s@/[^/]*$@@;
    $dir .= '/';

    my %pmap = ();
    my $id = 0;

    @lines = ();
    %xmap = ();
    push(@lines, "## ========================================");
    push(@lines, "## Auto-generated from ttlite file");
    push(@lines, "## ========================================");
    push(@lines, "## ** do not edit the file, edit the source **");
    open(F,$fn) || die $fn;
    while(<F>) {
        chomp;
        if (m@^\#FAIL\s+(\S+)@) {
            my $x = $1;
            $xmap{$x} = [@lines, '# DELIBERATE ERROR BELOW:'];
            while(<F>) {
                if (m@^\#END@) {
                    push(@{$xmap{$x}}, '# END OF DELIBERATE ERROR');
                    last;
                }
                chomp;
                s@^\#\s*@@;
                push(@{$xmap{$x}}, $_);
            }
            next;
            
        }
        
        if (m@^include (.*)@) {
            open(F2,"$dir$1") || die $1;
            while (<F2>) {
                chomp;
                push(@lines, $_);
            }
            close(F2);
            next;
        }
        if (m/\@prefix (\S+): (\S+):(\S+) ! .*/i) {
            $_ = "\@prefix $1: <http://purl.obolibrary.org/obo/$2_$3> .";
        }
        if (m@^(\S+): a (.*)@) {
            $_ = "$1: a owl:NamedIndividual, $2";
        }

        if (m@^(\S+\d+):@) {
            my $n = $1;
            if (!$pmap{$n}) {
                $id++;
                $pmap{$n} = "http://model.geneontology.org/$id";
            }
        }
        addline($_);
    }
    close(F);
    foreach my $p (keys %pmap) {
        my $pline = "\@prefix $p: <$pmap{$p}> .";
        unshift(@lines, $pline);
        foreach my $x (keys %xmap) {
            unshift(@{$xmap{$x}}, $_);        
        }

    }
    foreach (@lines) {
        print "$_\n";
    }
    foreach my $x (keys %xmap) {
        my $ffn = $fn;
        $ffn =~ s@p-(.*)\.ttlite@f-$1-$x.ttlite@;
        open(OF, ">$ffn") || die $ffn;
        foreach (@{$xmap{$x}}) {
            print OF "$_\n";
        }
        close(OF);
    }
}


exit 0;

sub addline {
    $_ = shift;
    push(@lines, $_);
    foreach my $x (keys %xmap) {
        push(@{$xmap{$x}}, $_);        
    }
}
