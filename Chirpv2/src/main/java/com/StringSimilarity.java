package com;

/*
 * Created by Daalekz
 * Checks similarity between two strings to provide suggestions
 */

class StringSimilarity
{
    //calculates a similarity index between 1 and 0
    static double similarity(String s1, String s2)
    {
        //make sure longer is always s2
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length())
        {
            longer = s2;
            shorter = s1;
        }

        //if longer is 0 length, both strings are 0 length, so they are a perfect match
        int longerLength = longer.length();
        if (longerLength == 0)
        {
            return 1.0;
        }

        //get index of similarity using Levenshtein
        //System.out.println(index + "this is the comparison index");//this line is for debugging
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    //implementation of the Levenshtein Edit Distance
    private static int editDistance(String s1, String s2)
    {
        //convert both strings to lowercase for testing
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        //idk how this works, its big brain, but it works
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++)
        {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++)
            {
                if (i == 0)
                {
                    costs[j] = j;
                }
                else
                {
                    if (j > 0)
                    {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                        {
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
            {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }
}