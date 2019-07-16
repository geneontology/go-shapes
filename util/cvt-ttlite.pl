#!/usr/bin/perl
use strict;

my %pmap = ();
my $id = 0;
my @lines = ();
my $dir = './';
if (@ARGV) {
    $dir = $ARGV[0];
    $dir =~ s@/[^/]*$@@;
    $dir .= '/';
}

print "## ========================================\n";
print "## Auto-generated from ttlite file\n";
print "## ========================================\n";
print "## ** do not edit the file, edit the source **\n";


while(<>) {
    chomp;
    if (m@^include (.*)@) {
        open(F,"$dir$1") || die $1;
        while (<F>) {
            chomp;
            push(@lines, $_);
        }
        close(F);
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
    
    push(@lines, $_);
}

foreach my $p (keys %pmap) {
    print "\@prefix $p: <$pmap{$p}> .\n";
}
foreach (@lines) {
    print "$_\n";
}
